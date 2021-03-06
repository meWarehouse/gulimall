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

        //?????? catelogId ??? 0 ???????????????
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
     * ????????????id?????????????????????????????????????????????????????????
     * @param catelogId
     * @return
     */
    @Transactional
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {

        /*
            ??? pms_attr_group ?????? ?????? ????????????????????????????????????
            ???????????????????????????????????? attr_group_id
            ????????? pms_attr_attrgroup_relation ???????????? attr_group_id ?????? ?????????????????????????????? attr_id
            ??????????????? attr_id ??? pms_attr ??????????????????????????????????????????
         */

        //1.??????????????????
        List<AttrGroupEntity> groupEntities = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //2.?????????????????????
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

        //????????????spu?????????????????????????????????????????????????????????????????????????????????
        /**
         * #1?????????spu?????????????????????????????? groupName,attrName,attrsValue
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