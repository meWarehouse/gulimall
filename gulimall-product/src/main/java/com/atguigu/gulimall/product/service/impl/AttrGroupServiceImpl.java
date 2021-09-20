package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.skuitem.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {


        String key = (String) params.get("key");

        //select * from `pms_attr_group` where catelog_id = ? and (attr_group_id = key or attr_group_name LIKE %key%)

        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();

        if(!StringUtils.isEmpty(key)){
            wrapper.and(obj -> {
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }

        //如果 catelogId 为 0 则查询所有
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);

        }else{

            wrapper.eq("catelog_id",catelogId);

            //SELECT attr_group_id,icon,catelog_id,sort,descript,attr_group_name FROM pms_attr_group WHERE (catelog_id = ? AND ( (attr_group_id = ? OR attr_group_name LIKE ?) ))
            //aa(String), aa(String), %aa%(String)

            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);

            return new PageUtils(page);

        }

    }

    /**
     * 根据分类id查出所有的属性分组以及这些组里面的属性
     * @param catelogId
     * @return
     */
    @Transactional
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {

        /*
            在 pms_attr_group 表中 查询 属于该分类的所有属性分组
            通过属性分组获取到所有的 attr_group_id
            然后在 pms_attr_attrgroup_relation 表中根据 attr_group_id 查询 属于该分组的所有属性 attr_id
            通过查出的 attr_id 在 pms_attr 表中查出属于给分组的属性详情
         */

        //1.获取所有分组
        List<AttrGroupEntity> groupEntities = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //2.查询所有的属性
        List<AttrGroupWithAttrsVo> collect = groupEntities.stream().map(attrGroupEntity -> {
            AttrGroupWithAttrsVo withAttrsVo = new AttrGroupWithAttrsVo();

            BeanUtils.copyProperties(attrGroupEntity,withAttrsVo);

            List<AttrEntity> attrs = attrService.getRelationAttr(withAttrsVo.getAttrGroupId());
            withAttrsVo.setAttrs(attrs);

            return withAttrsVo;
        }).collect(Collectors.toList());


        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId,Long catalogId) {

        //查找当前spu对应的所有属性的分组信息及当前分组下的所有属性对应的值
        /**
         * #1，当前spu有多少对应的属性分组 groupName,attrName,attrsValue
         * SELECT
         * 	pav.`spu_id`,
         * 	ag.`attr_group_name`,
         * 	ag.`attr_group_id`,
         * 	aar.`attr_id`,
         * 	attr.`attr_name`,
         * 	pav.`attr_value`
         * 	FROM `pms_attr_group` ag
         * LEFT JOIN `pms_attr_attrgroup_relation` aar ON  aar.`attr_group_id` = ag.`attr_group_id`
         * LEFT JOIN `pms_attr` attr ON attr.`attr_id` = aar.`attr_id`
         * LEFT JOIN `pms_product_attr_value` pav ON pav.`attr_id` = attr.`attr_id`
         * WHERE ag.catelog_id = 225 AND pav.`spu_id` = 17
         */
        return baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);

    }

}