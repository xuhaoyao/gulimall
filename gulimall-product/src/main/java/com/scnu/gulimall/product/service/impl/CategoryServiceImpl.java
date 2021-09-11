package com.scnu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.scnu.common.utils.Query;
import com.scnu.gulimall.product.constant.RedisConstant;
import com.scnu.gulimall.product.constant.RedissonConstant;
import com.scnu.gulimall.product.dao.CategoryBrandRelationDao;
import com.scnu.gulimall.product.vo.Catalog2Vo;
import javafx.collections.MapChangeListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;

import com.scnu.gulimall.product.dao.CategoryDao;
import com.scnu.gulimall.product.entity.CategoryEntity;
import com.scnu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listTree() {
        //1.查所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.找出父菜单,组装成树形结构
        List<CategoryEntity> tree = entities.stream()
                .filter(category -> category.getParentCid() == 0)
                .map(menu1 -> {
                    menu1.setChildren(getChildren(menu1,entities));
                    return menu1;
                })
                .sorted((o1, o2) -> o1.getSort() - o2.getSort())
                .collect(Collectors.toList());
        return tree;
    }

    @Transactional
    @Override
    public void removeBatch(List<Long> ids) {
        //TODO 该菜单被引用时不能删除
        baseMapper.deleteBatchIds(ids);
    }

    @Override
    public Long[] getCatelogIdPath(Long catelogId) {
        List<Long> list = new LinkedList<>();
        findPath(catelogId,list);
        Collections.reverse(list);  //递归找出来后,逆序返回
        return list.toArray(new Long[0]);
    }

    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        Long catId = category.getCatId();
        String name = category.getName();
        categoryBrandRelationDao.updateFromCategory(catId,name);
        baseMapper.updateById(category);
    }

    @Override
    public List<CategoryEntity> getLevelOneList() {
        List<CategoryEntity> categoryEntities = this.getLevelListWithCondition(0L,1);
        return categoryEntities;
    }

    /**
     * 找出父分类id下对应的所有分类
     * 由于此语句大量查询操作,给它加索引
     * @param parentCid 父分类id
     * @param level     分类等级
     * @return
     */
    private List<CategoryEntity> getLevelListWithCondition(Long parentCid,Integer level){
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid).eq("cat_level",level));
        return categoryEntities;
    }

    /**
     * TODO 这种写法性能太差了,看看老师后面有没有什么操作
     * TODO ->可以一次查所有记录,然后在自己拼数据
     * @return
     */
/*    @Override
    public Map<String, Object> getcatalogJson_bad() {
        Long start = System.currentTimeMillis();
        //1.向查一级分类
        List<CategoryEntity> levelOneList = this.getLevelOneList();
        Map<String, Object> collect = levelOneList.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), levelOne -> {
            //查这个一级分类对应的所有二级分类
            List<CategoryEntity> levelTwoList = this.getLevelListWithCondition(levelOne.getCatId(), levelOne.getCatLevel() + 1);
            //封装成对应的vo2
            List<Catalog2Vo> catalog2Vos = levelTwoList.stream().map(levelTwo -> {
                Catalog2Vo catalog2Vo = new Catalog2Vo(levelOne.getCatId().toString(), null, levelTwo.getCatId().toString(), levelTwo.getName());
                //查这个二级分类对应的所有三级分类
                List<CategoryEntity> levelThreeList = this.getLevelListWithCondition(levelTwo.getCatId(), levelTwo.getCatLevel() + 1);
                //封装成对应的vo3
                List<Catalog2Vo.Catalog3Vo> catalog3List = levelThreeList.stream().map(levelThree -> {
                    Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(levelTwo.getCatId().toString(), levelThree.getCatId().toString(), levelThree.getName());
                    return catalog3Vo;
                }).collect(Collectors.toList());
                catalog2Vo.setCatalog3List(catalog3List);
                return catalog2Vo;
            }).collect(Collectors.toList());
            return catalog2Vos;
        }));
        Long end = System.currentTimeMillis();
        System.out.println("耗时:" + (end - start) + "ms");
        return collect;
    }*/

    /**
     * 虚拟机下redis
     * io.netty.util.internal.OutOfDirectMemoryError:
     * failed to allocate 46137344 byte(s) of direct memory (used: 58720256, max: 100663296)
     * TODO 产生堆外内存溢出:OutOfDirectMemoryError
     * 1)SpringBoot2.0以后默认使用lettuce作为操作redis的客户端。使用netty进行网络通信
     * 2)lettuce的bug导致netty堆外内存溢出 -Xmx100m netty如果没有指定堆外内存,默认使用-Xmx300m
     *      可以通过-Dio.netty.maxDirectMemory进行设置
     *  解决方案:不能使用-Dio.netty.maxDirectMemory只去调大堆外内存
     *      1)升级lettuce客户端
     *      2)切换使用jedis(此处使用)
     *
     *  jedis和lettuce都是操作redis的底层客户端
     *  而SpringBoot将它们再次封装
     *  @Import({LettuceConnectionConfiguration.class, JedisConnectionConfiguration.class})
     *  public class RedisAutoConfiguration
     *
     *
     *
     * 云服务器由于带宽的限制,演示不出这个效果
     */
    @Override
    public Map<String, Object> getcatalogJson(){

        /**
         * 1.空结果缓存,解决缓存穿透
         * 2.设置过期时间(加随机值),解决缓存雪崩
         * 3.加锁:解决缓存击穿
         */

        //给缓存中放json字符串,拿出的json字符串还方便解析(jackson)
        //json跨语言跨平台
        String catalogJson = stringRedisTemplate.opsForValue().get(RedisConstant.CATALOGJSON);
        Map<String, Object> catalog = null;
        if(StringUtils.isEmpty(catalogJson)){
            System.out.println("缓存不命中...查询数据库");
            catalog = this.getcatalogJsonWithRedissonLock(); //this.getcatalogJsonWithRedisLock(); //this.getcatalogJsonSynchronized();
            return catalog;
        }
        catalog = JSON.parseObject(catalogJson,new TypeReference<Map<String, Object>>(){});
        //catalog = JSON.parseObject(catalogJson, Map.class);
        System.out.println("缓存命中...");
        return catalog;
    }

    /**
     * Redisson分布式锁
     * 阻塞式等待
     */
    public Map<String,Object> getcatalogJsonWithRedissonLock(){
        RLock lock = redisson.getLock(RedissonConstant.CATALOGLOCK);
        Map<String,Object> result;
        try {
            lock.lock(30L,TimeUnit.SECONDS);
            result = this.getcatalogJsonSynchronized();
        }finally {
            lock.unlock();
        }
        return result;
    }

    /**
     * Redis分布式锁
     * 自旋式等待
     */
    public Map<String,Object> getcatalogJsonWithRedisLock(){
        String uuid = UUID.randomUUID().toString();
        Boolean flag;
        do{
            //300s秒过期,保证了在该业务执行期间锁过期的问题
            flag = stringRedisTemplate.opsForValue().setIfAbsent(RedisConstant.CATALOGLOCK, uuid, 300L, TimeUnit.SECONDS);
            //若抢到了锁
            if(flag){
                Map<String, Object> result = null;
                try{
                    result = this.getcatalogJsonSynchronized();
                }finally {
                    String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                    //lua脚本 -> 获取值成功+对比成功删除 -> 原子删除
                    stringRedisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class), Arrays.asList(RedisConstant.CATALOGLOCK), uuid);
                }
                return result;
            }
        }while (!flag);  //自旋
        return null;
    }


    /**
     * 本地锁解决缓存击穿
     */
    public Map<String, Object> getcatalogJsonSynchronized() {
        synchronized (this){
            //double checked
            String catalogJson = stringRedisTemplate.opsForValue().get(RedisConstant.CATALOGJSON);
            if(StringUtils.hasLength(catalogJson)){
                Map<String, Object> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, Object>>() {});
                return result;
            }
            return this.getCatalogJsonFromDb();
        }

    }

    /**
     * 缓存中无数据,查数据库,同时将数据放入redis
     */
    private Map<String, Object> getCatalogJsonFromDb(){
        Long start = System.currentTimeMillis();
        //1.查所有数据
        List<CategoryEntity> allList = baseMapper.selectList(null);
        List<CategoryEntity> levelOneList = this.getLevelListWithCondition(allList,0L,1);
        Map<String, Object> collect = levelOneList.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), levelOne -> {
            //查这个一级分类对应的所有二级分类
            List<CategoryEntity> levelTwoList = this.getLevelListWithCondition(allList,levelOne.getCatId(), levelOne.getCatLevel() + 1);
            //封装成对应的vo2
            List<Catalog2Vo> catalog2Vos = levelTwoList.stream().map(levelTwo -> {
                Catalog2Vo catalog2Vo = new Catalog2Vo(levelOne.getCatId().toString(), null, levelTwo.getCatId().toString(), levelTwo.getName());
                //查这个二级分类对应的所有三级分类
                List<CategoryEntity> levelThreeList = this.getLevelListWithCondition(allList,levelTwo.getCatId(), levelTwo.getCatLevel() + 1);
                //封装成对应的vo3
                List<Catalog2Vo.Catalog3Vo> catalog3List = levelThreeList.stream().map(levelThree -> {
                    Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(levelTwo.getCatId().toString(), levelThree.getCatId().toString(), levelThree.getName());
                    return catalog3Vo;
                }).collect(Collectors.toList());
                catalog2Vo.setCatalog3List(catalog3List);
                return catalog2Vo;
            }).collect(Collectors.toList());
            return catalog2Vos;
        }));
        Long end = System.currentTimeMillis();
        System.out.println("数据库查询分类耗时:" + (end - start) + "ms");
        String catalogJson = JSON.toJSONString(collect);
        //将值set去redis应该在synchronized代码块中执行,而不是离开了同步代码块再执行(想想为什么)
        stringRedisTemplate.opsForValue().set(RedisConstant.CATALOGJSON,catalogJson,1, TimeUnit.DAYS);
        return collect;
    }

    private List<CategoryEntity> getLevelListWithCondition(List<CategoryEntity> allList,Long parentCid,Integer level){
        List<CategoryEntity> collect =
                allList.stream()
                        .filter(item -> item.getParentCid().equals(parentCid) && item.getCatLevel().equals(level))
                        .collect(Collectors.toList());
        return collect;
    }



    private void findPath(Long catelogId, List<Long> list) {
        list.add(catelogId);
        CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        Long pid = categoryEntity.getParentCid();
        if(pid != 0){
            findPath(pid,list);
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity menu, List<CategoryEntity> entities) {
        List<CategoryEntity> tree = entities.stream()
                .filter(subMenu -> subMenu.getParentCid().equals(menu.getCatId()))
                .map(subMenu -> {
                    subMenu.setChildren(getChildren(subMenu, entities));
                    return subMenu;
                })
                .sorted((o1, o2) -> o1.getSort() - o2.getSort())
                .collect(Collectors.toList());
        return tree;
    }


}