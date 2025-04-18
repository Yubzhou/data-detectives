--[[
Redis 安全批量删除脚本（支持动态参数）
参数说明：
ARGV[1] - 键匹配模式（必填，如："news:*"）
ARGV[2] - 单次扫描数量（可选，默认500）
ARGV[3] - 批量删除数量（可选，默认100）

返回值：删除的键总数
--]]

--local pattern = ARGV[1] -- 模式参数（必填）
local rawPattern = ARGV[1] or ""
-- 清理 pattern：去首尾空格 + 去双引号
local pattern = rawPattern:gsub("^%s*(.-)%s*$", "%1"):gsub('^"(.*)"$', "%1")
if pattern == "" then
    return redis.error_reply("Missing required parameter: key pattern (ARGV[1])")
end

local scanCount = tonumber(ARGV[2]) or 500
if not scanCount or scanCount < 1 then
    scanCount = 500
end

local batchSize = tonumber(ARGV[3]) or 100
if not batchSize or batchSize < 1 then
    batchSize = 100
end

-- 检测是否支持 UNLINK
local unlinkSupported = redis.call('COMMAND', 'INFO', 'UNLINK')[1] ~= nil
local deleteCommand = unlinkSupported and 'UNLINK' or 'DEL'

-- 添加日志以验证 deleteCommand 参数
--redis.log(redis.LOG_WARNING, "deleteCommand: " .. deleteCommand)

local cursor = 0 -- 初始化游标
local deleted = 0 -- 删除计数器

-- 添加日志以验证 pattern 参数
--redis.log(redis.LOG_WARNING, "Pattern received: " .. pattern)

repeat
    -- 执行 SCAN 命令（推荐使用非阻塞的 UNLINK）
    local result = redis.call('SCAN', cursor, 'MATCH', pattern, 'COUNT', scanCount)
    --local result = redis.call('SCAN', cursor, 'MATCH', "news:detail:*", 'COUNT', scanCount)
    cursor = tonumber(result[1])
    local keys = result[2]
    ----记录完整游标和当前批次所有键
    --redis.log(redis.LOG_WARNING, "SCAN cursor: " .. result[1])
    --if #keys > 0 then
    --    redis.log(redis.LOG_WARNING, "Keys in batch: " .. table.concat(keys, ", "))
    --else
    --    redis.log(redis.LOG_WARNING, "No keys found in this batch.")
    --end

    -- 删除键
    if #keys > 0 then
        -- 分批删除避免单命令参数过多
        for i = 1, #keys, batchSize do
            local endIndex = math.min(i + batchSize - 1, #keys)
            redis.call(deleteCommand, unpack(keys, i, endIndex))
        end
        deleted = deleted + #keys
    end

until cursor == 0

return deleted