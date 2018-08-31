package com.redis.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
@Component
public class RedisUtils {
    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 把key-value对写入redis，并制定expire duration
     * 
     * @param key
     * @param value
     * @param milliseconds
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void store(final Object key, final Object value, final long milliseconds) throws Exception {
        if (key == null || value == null) {
            return;
        }
        try{
        redisTemplate.execute(new RedisCallback<Object>() {
            RedisSerializer valueSerializer = redisTemplate.getStringSerializer();
            RedisSerializer keySerializer = redisTemplate.getStringSerializer();
            byte[] keyByte = keySerializer.serialize(key);
            byte[] valueByte = valueSerializer.serialize(value);

            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                if (milliseconds > 0) {
                    connection.pSetEx(keyByte, milliseconds, valueByte);
                } else {
                    connection.set(keyByte, valueByte);
                }
                return null;
            }
        });
        }catch(Exception e){
        	e.printStackTrace(); 
        }
        
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean exists(final Object key) throws Exception {
        if (key == null) {
            return false;
        }
        Object obj = redisTemplate.execute(new RedisCallback<Boolean>() {
            RedisSerializer keySerializer = redisTemplate.getStringSerializer();

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyByte = keySerializer.serialize(key);
                boolean exists = connection.exists(keyByte);
                return exists;
            }
        });
        if (obj == null) {
            return false;
        } else {
            return (Boolean) obj;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String get(final Object key) throws Exception {
        if (key == null) {
            return null;
        }
        Object resultObject = redisTemplate.execute(new RedisCallback<String>() {
            RedisSerializer valueSerializer = redisTemplate.getStringSerializer();
            RedisSerializer keySerializer = redisTemplate.getStringSerializer();

            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                String value = null;
                byte[] keyByte = keySerializer.serialize(key);
                byte[] valueByte = connection.get(keyByte);
                if (valueByte != null) {
                    Object o = valueSerializer.deserialize(valueByte);
                    if (o != null) {
                        value = (String) o;
                    }
                }
                return value;
            }
        });
        if (resultObject == null) {
            return null;
        } else {
            return (String) resultObject;
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void delete(final Object key) throws Exception {
        if (key == null) {
            return;
        }
        redisTemplate.execute(new RedisCallback<Object>() {
            RedisSerializer keySerializer = redisTemplate.getStringSerializer();
            byte[] keyByte = keySerializer.serialize(key);

            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.del(keyByte);
                return null;
            }

        });
    }

    /**
     * 先遍历所有k-v，然后删除所有以某个字符串开始的key
     * 
     * @param regExp
     */
    @SuppressWarnings("unchecked")
    public void scanAndDeleteByPrefix(final List<String> prefixList) {
        if (prefixList == null || prefixList.size() == 0) {
            return;
        }

        JedisConnection jedisConn = (JedisConnection) redisTemplate.getConnectionFactory().getConnection();

        final List<byte[]> batchKeys = new LinkedList<byte[]>();
        final List<byte[]> needDelKeys = new LinkedList<byte[]>();

        redisTemplate.setValueSerializer(redisTemplate.getStringSerializer());
        long cursorId = 0;
        final int batchSize = 100;
        do {
            Cursor<byte[]> cursor = jedisConn.scan(cursorId, null);
            while (cursor.hasNext()) {
            	byte[] key = cursor.next();
            	if(!"VENDER_INFO".equals(new String(key)))
            		batchKeys.add(key);

                if (batchKeys.size() == batchSize) {
                    List<String> values = redisTemplate.executePipelined(new RedisCallback<String>() {
                        @Override
                        public String doInRedis(RedisConnection connection) throws DataAccessException {
                            for (byte[] b : batchKeys) {
                            	connection.get(b);
                            }

                            return null;
                        }
                    });

                    for (int i = 0, size = values.size(); i < size; i++) {
                        if (values.get(i) != null) {
                            for (String s : prefixList) {
                                if (values.get(i).startsWith(s)) {
                                    needDelKeys.add(batchKeys.get(i));
                                    break;
                                }
                            }
                        }
                    }

                    if (needDelKeys.size() > 0) {
                        jedisConn.del(needDelKeys.toArray(new byte[needDelKeys.size()][]));
                        needDelKeys.clear();
                    }
                    batchKeys.clear();
                }
            }

            cursorId = cursor.getCursorId();
        } while (cursorId != 0);
        
        if (batchKeys.size() > 0) {
            List<String> values = redisTemplate.executePipelined(new RedisCallback<String>() {
                @Override
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                    for (byte[] b : batchKeys) {
                    	connection.get(b);
                    }

                    return null;
                }
            });

            for (int i = 0, size = values.size(); i < size; i++) {
                if (values.get(i) != null) {
                    for (String s : prefixList) {
                        if (values.get(i).startsWith(s)) {
                            needDelKeys.add(batchKeys.get(i));
                            break;
                        }
                    }
                }
            }

            if (needDelKeys.size() > 0) {
                jedisConn.del(needDelKeys.toArray(new byte[needDelKeys.size()][]));
                needDelKeys.clear();
            }
        }
    }

    /**
     * 基于正则表达式批量删除key
     * 
     * @param regExp
     */
    @SuppressWarnings({ "unchecked" })
    public void deleteByRegExp(final String regExp) {
        if (StringUtils.isEmpty(regExp)) {
            return;
        }

        final RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();

        redisTemplate.execute(new RedisCallback<List<String>>() {
            @Override
            public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
                Set<byte[]> keyBytes = connection.keys(keySerializer.serialize(regExp));

                connection.del(keyBytes.toArray(new byte[keyBytes.size()][]));

                return null;
            }
        });
    }

    /**
     * 
     * 从redis的hash表中删除不存在的key，然后更新已经存在的key <br>
     * 
     * @param map
     *        待更新key列表
     */
    @SuppressWarnings("unchecked")
    public void hashSetAndDeleteKeys(final String key, final Map<String, String> map) {
        redisTemplate.execute(new RedisCallback<Void>() {
            @Override
            public Void doInRedis(RedisConnection connection) throws DataAccessException {
            	Set<byte[]> keys = connection.hKeys(key.getBytes());
                List<byte[]> deleteKey = new ArrayList<byte[]>();
                for (byte[] b : keys) {
                    if (!map.containsKey(new String(b))) {
                        deleteKey.add(b);
                    }
                }
                
                if (deleteKey.size() > 0)
                    connection.hDel(key.getBytes(), deleteKey.toArray(new byte[0][]));
                
                Map<byte[], byte[]> kvMap = new HashMap<byte[], byte[]>(map.size());
                for (Entry<String, String> entry : map.entrySet()) {
                    kvMap.put(entry.getKey().getBytes(), entry.getValue().getBytes());
                }
                
                if(!kvMap.isEmpty())
                	connection.hMSet(key.getBytes(), kvMap);
                
                return null;
            }
        });
    }
}
