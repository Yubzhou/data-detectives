--[[
核心目标：从ZSET中随机获取N个不在排除集合中的元素
实现方式：循环随机抽取 + 排除检查 + 结果去重

参数说明：
KEYS[1] = ZSet键名（热门新闻ZSet）
KEYS[2] = 排除集合的键名（用户已推荐过的新闻ID集合）
KEYS[3] = 所属某新闻分类的新闻ID集合键名
ARGV[1] = 新闻分类ID（当其为0时表示任意类型新闻）
ARGV[2] = 需要获取的数量
ARGV[3] = 最大尝试次数（避免死循环）
ARGV[4] = 排除集合的过期时间（单位为秒）

返回值：
返回值是一个数组，数组中的元素为ZSET中随机抽取的元素（去重）
--]]

local result = {} -- 存储结果的数组
local added = {} -- 用于结果去重（防止result数组里面添加了重复元素）
local categoryId = tonumber(ARGV[1])
local need = tonumber(ARGV[2])
local zSetSize = redis.call('ZCARD', KEYS[1])
local excludeSetSize = redis.call('SCARD', KEYS[2])

-- 如果 ( (需要获取的数量+排除集合的元素数量) / ZSET元素数量 ) > 0.8，则直接返回空数组（因为如果继续查询效率太低）
if ((need + excludeSetSize) / zSetSize) > 0.8 then
    return result
end

-- 计算需要获取的数量，并限制小于ZSET和排除集合的元素数量之差
local count = math.min(need, zSetSize - excludeSetSize)
-- 如果可获取的数量小于等于需要获取数量的一半，则直接返回空数组（因为如果继续查询效率太低）
if count <= need / 2 then
    return result
end

local maxAttempts = tonumber(ARGV[3]) or count * 3 -- 最大尝试次数，默认为需要获取的数量的3倍
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
        local isInCategory = 1 -- 默认可以添加
        -- 检查是否在指定分类中
        if categoryId ~= 0 then
            isInCategory = redis.call('SISMEMBER', KEYS[3], member)
        end
        -- 如果不在排除集合中 and 属于指定新闻分类 and 未添加到result过
        if isExcluded == 0 and isInCategory == 1 and not added[member] then
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
    local ttl = redis.call('TTL', KEYS[2])
    -- 仅当集合未设置过期时间时设置
    if ttl == -1 then
        -- 从 ARGV 中获取过期时间（默认为3小时）
        local expireSeconds = tonumber(ARGV[4]) or 10800
        redis.call('EXPIRE', KEYS[2], expireSeconds)
    end
end

-- 返回数组结果
return result