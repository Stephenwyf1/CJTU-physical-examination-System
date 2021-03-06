package com.company.project.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.project.entity.BloodEntity;
import com.company.project.entity.StuTestEntity;
import com.company.project.entity.StudentEntity;
import com.company.project.mapper.BloodMapper;
import com.company.project.mapper.DoctorMapper;
import com.company.project.mapper.StuTestMapper;
import com.company.project.mapper.StudentMapper;
import com.company.project.service.IBloodService;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 血压脉搏 服务实现类
 * </p>
 *
 * @author wyf
 * @since 2021-06-06
 */
@Service
public class BloodServiceImpl extends ServiceImpl<BloodMapper, BloodEntity> implements IBloodService {
    @Resource
    private BloodMapper bloodMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private StuTestMapper stuTestMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getStuInfoList(int Stu_id) {
        String sql;
        if(Stu_id == -1)
        {
            sql = "select Student.*, (case when Blood_error is NULL then '0' "
                    +"when Blood_error = '1' then '2' "
                    +"else '1' end)Blood_all "
                    +"from Student left join Blood "
                    +"on Student.Stu_id = Blood.Stu_id;";
        }
        else
        {
            sql = "select s.*, (case when Blood_error is NULL then '0' "
                    +"when Blood_error = '1' then '2' "
                    +"else '1' end)Blood_all "
                    +"from (select * from Student where Stu_id = "+Stu_id+") as s "
                    +"left join Blood "
                    +"on s.Stu_id = Blood.Stu_id;";
        }
        return jdbcTemplate.queryForList(sql);
}

    @Override
    public List<Map<String, Object>> getStuBloodInfo(int Stu_id) {
        LambdaQueryWrapper<BloodEntity> BloodQueryWrapper = Wrappers.lambdaQuery();
        BloodQueryWrapper.eq(BloodEntity::getStuId, Stu_id);
        return bloodMapper.selectMaps(BloodQueryWrapper);
    }

    @Override
    public void insertStuBloodInfo(BloodEntity bloodEntity) {
        StuTestEntity stuTestEntity = new StuTestEntity();
        boolean bFirstInsert;


        System.out.println( bloodMapper.selectById(bloodEntity.getStuId()) );

        if( bloodMapper.selectById(bloodEntity.getStuId()) == null )
        {
            bloodMapper.insert(bloodEntity);
            bFirstInsert = true;
        }
        else
        {
            bloodMapper.updateById(bloodEntity);
            bFirstInsert = false;
        }

        //插入Blood表的同时要把部分数据插入到StuTest表
        stuTestEntity.setStuId(bloodEntity.getStuId());
        stuTestEntity.setBloodIdea(bloodEntity.getBloodIdea());
        stuTestEntity.setBloodDoctorName(doctorMapper.selectById(bloodEntity.getBloodDoctorId()).getDoctorName());
        stuTestEntity.setBloodDoctorId(bloodEntity.getBloodDoctorId());
        stuTestEntity.setBloodOperationTime(bloodEntity.getBloodOperationTime());

        StuTestEntity selectEntity = stuTestMapper.selectById(bloodEntity.getStuId());

        if(selectEntity == null)//if StuTest 没有数据
        {
            stuTestEntity.setStuTestCount(1);
            stuTestMapper.insert(stuTestEntity);
        }
        else
        {
            if(bFirstInsert)//if first insert Entity then StuTestCount + 1
            {
                stuTestEntity.setStuTestCount(selectEntity.getStuTestCount() + 1);
            }
            stuTestMapper.updateById(stuTestEntity);
        }

    }

}
