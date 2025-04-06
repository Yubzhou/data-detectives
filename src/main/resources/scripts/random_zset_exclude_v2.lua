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
local batchSize = math.min(10, count)  -- 动态批量大小
local attempts = 0 -- 尝试次数计数器

-- 预加载排除集合（适用于排除集合较小的情况）
local excludeSet = {}
-- for index, value in ipairs(arr)：遍历数组（顺序索引）
for _, member in ipairs(redis.call('SMEMBERS', KEYS[2])) do
    excludeSet[member] = true
end

while #result < count and attempts < maxAttempts do
    -- 批量获取随机元素（Redis 6.2+特性）
    local candidates = redis.call('ZRANDMEMBER', KEYS[1], batchSize)

    -- 过滤候选元素
    -- for index, value in ipairs(arr)：遍历数组（顺序索引）
    for _, member in ipairs(candidates) do
        -- 如果不在排除集合中且未添加到result过
        if not excludeSet[member] and not added[member] then
            table.insert(result, member)
            added[member] = true
            -- 如果所需个数已满足，则提前结束循环
            if #result == count then
                break
            end
        end
    end

    -- 动态调整批量大小
    batchSize = math.min(count - #result, batchSize * 2)
    attempts = attempts + 1
end

-- 将新增元素添加到排除集合
-- 检查added是否为空
if next(added) ~= nil then
    local membersToAdd = {}
    -- 将added转为数组
    -- for key, value in pairs(hash)：遍历哈希表（非连续键），遍历表中所有键值对
    for member, _ in pairs(added) do
        table.insert(membersToAdd, member)
    end
    redis.call('SADD', KEYS[2], unpack(membersToAdd))
end

-- 返回数组结果
return result