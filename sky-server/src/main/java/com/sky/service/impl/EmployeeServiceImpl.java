package com.sky.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }
    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    public void save(EmployeeDTO employeeDTO) {
        //进行对象转换，把DTO对象属性 复制到实体对象中
        Employee employee = new Employee();
        //应为两个类中存在同样的属性，通过属性拷贝
        //从employeeDTO中拷贝到employee中
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置账号状态，默认正常状态 1正常，0锁定
        employee.setStatus(StatusConstant.ENABLE);//从StatusConstant常量类中获取状态常量，而不是选择硬编码
        //设置密码，默认密码为123456，但是要进行MD5加密
        //通过密码的常量类中获取密码常量123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置当前记录的创建时间和修改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //设置当前记录创建人id和修改人id
//        //TODO 后期完善，目前先设置为10L
//        //从ThreadLocal线程当中提取出之前存进去的empid
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        //调用持久层，对数据库进行插入
        employeeMapper.insert(employee);

    }
    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */

    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //通过pageHelper进行分页查询
        //开始分页查询
        //PageHelper.startPage(当前页码,每页显示的条数)把前端获取的参数传给PageHelper
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        //调用Mapper层的查询方法sql语句,获取到分页查询的结果，并存储于在Page<>中
        Page<Employee> page =employeeMapper.pageQuery(employeePageQueryDTO);
        //把Page对象处理为
        // 获取总条数
        // 获取当前页的结果集
        Long total = page.getTotal();
        List<Employee> records = page.getResult();
        //封装到PageResult对象中
        return new PageResult(total, records);
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     * @return
     */
    public void startOrStop(Integer status, Long id) {
        //update employee set status ==？ where id =？
        //创建实体对象
//        Employee employee = new Employee();
//        employee.setStatus(status);
//        employee.setId(id);
        //通过构建器创建对象也可以
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        //通过id查询员工信息
        Employee employee = employeeMapper.getById(id);
        //设置密码为******，不返回密码,应为密码经过加密
        employee.setPassword("******");
        //返回employee对象
        return employee;
    }

    /**
     * 编辑员工信息
     *
     * @param employeeDTO
     * @return
     */
    public void update(EmployeeDTO employeeDTO) {
        //利用在xml映射文件中创建的update方法，去进行编辑员工信息
        //但是需要把DTO对象转换为Employee对象才能够使用
        Employee employee = new Employee();
        //通过BeanUtils工具类进行对象属性拷贝，前者为源对象，后者为目标对象
        BeanUtils.copyProperties(employeeDTO, employee);

//        //设置修改时间，修改人id，这两个属性在DTO中没有，所以需要手动设置
//        employee.setUpdateTime(LocalDateTime.now());
//        //通过BaseContext工具类，底层为ThreadLocal 获取当前修改的用户的id
//        employee.setUpdateUser(BaseContext.getCurrentId());
        //调用持久层的update方法
        employeeMapper.update(employee);
    }

}
