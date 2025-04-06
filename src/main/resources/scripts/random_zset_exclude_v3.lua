-- 核心目标：从ZSET中随机获取N个不在排除集合中的元素
-- 实现方式：循环随机抽取 + 排除检查 + 结果去重

-- KEYS[1] = ZSET键名
-- KEYS[2] = 排除集合的键名
-- ARGV[1] = 需要获取的数量
-- ARGV[2] = 最大尝试次数（避免死循环）

local result = {} -- 存储结果的数组
local added = {} -- 用于结果去重（防止result数组里面添加了重复元素）
-- 计算需要获取的数量，并限制小于ZSET和排除集合的元素数量之差
local count = math.min(
        tonumber(ARGV[1]),
        redis.call('ZCARD', KEYS[1]) - redis.call('SCARD', KEYS[2])
)
-- 如果需要获取的数量小于等于0，则直接返回空数组
if count <= 0 then
    return result
end

local maxAttempts = tonumber(ARGV[2]) or count * 5 -- 最大尝试次数，默认为需要获取的数量的5倍
local batchSize = math.min(count, 10)  -- 动态批量大小
local attempts = 0 -- 尝试次数计数器

while #result < count and attempts < maxAttempts do
    -- 批量获取随机元素（Redis 6.2+特性）
    local candidates = redis.call('ZRANDMEMBER', KEYS[1], batchSize)

    -- 过滤候选元素
    -- for index, value in ipairs(arr)：遍历数组（顺序索引）
    for _, member in ipairs(candidates) do
        -- 检查是否在排除集合中
        local isExcluded = redis.call('SISMEMBER', KEYS[2], member)
        -- 如果不在排除集合中且未添加到result过
        if isExcluded == 0 and not added[member] then
            table.insert(result, member)
            added[member] = true
            -- 如果所需个数已满足，则提前结束循环
            if #result == count then
                break
            end
        end
    end

    -- 动态调整批量大小（最小为2，最大50）
    batchSize = math.max(2, math.min(count - #result, batchSize * 2, 50))
    attempts = attempts + 1
end

-- 分批次将新增元素添加到排除集合（避免超过Redis命令行工具限制）
-- 检查added是否为空
if next(added) ~= nil then
    local membersToAdd = {}
    -- 将added转为数组
    -- for key, value in pairs(hash)：遍历哈希表（非连续键），遍历表中所有键值对
    for member, _ in pairs(added) do
        table.insert(membersToAdd, member)
    end
    local chuckSize = 100 -- 每批次添加的元素数量
    -- 数组下标从1开始
    -- for i = 1, #membersToAdd, chuckSize：从1开始，每次迭代增加chuckSize，直到数组长度结束
    for i = 1, #membersToAdd, chuckSize do
        -- #membersToAdd：数组长度
        local endIdx = math.min(i + chuckSize - 1, #membersToAdd)
        redis.call('SADD', KEYS[2], unpack(membersToAdd, i, endIdx))
    end
end

-- 返回数组结果
return result