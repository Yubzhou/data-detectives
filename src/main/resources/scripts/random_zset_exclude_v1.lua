-- 核心目标：从ZSET中随机获取N个不在排除集合中的元素
-- 实现方式：循环随机抽取 + 排除检查 + 结果去重

-- KEYS[1] = ZSET键名
-- KEYS[2] = 排除集合的键名
-- ARGV[1] = 需要获取的数量
-- ARGV[2] = 最大尝试次数（避免死循环）

local result = {} -- 存储结果的数组
local selected = {} -- 用于结果去重（防止result数组里面添加了重复元素）
local count = tonumber(ARGV[1]) -- 需要获取的数量
local maxAttempts = tonumber(ARGV[2]) or count * 3 -- 最大尝试次数，默认为需要获取的数量的三倍
local attempts = 0 -- 尝试次数计数器

-- 获取ZSet的元素数量和Set的元素数量之差
local zSetSize = redis.call('ZCARD', KEYS[1])
local setSize = redis.call('SCARD', KEYS[2])
-- count取最小值
count = math.min(count, zSetSize - setSize)

while #result < count and attempts < maxAttempts do
    -- 从ZSet随机获取一个元素
    local randomMember = redis.call('ZRANDMEMBER', KEYS[1])
    if randomMember then
        -- 检查是否在排除集合中
        local isExcluded = redis.call('SISMEMBER', KEYS[2], randomMember)
        -- 如果不在排除集合中且未添加到result过，则添加到结果中
        if isExcluded == 0 and not selected[randomMember] then
            table.insert(result, randomMember)
            selected[randomMember] = true
        end
    end
    attempts = attempts + 1
end

-- 返回数组结果
return result