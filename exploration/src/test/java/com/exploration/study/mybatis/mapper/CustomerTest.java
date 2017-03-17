package com.exploration.study.mybatis.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.study.mybatis.mapper.CustomerMapper;
import com.study.mybatis.model.Customer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={ "classpath:spring/applicationContext.xml" })
public class CustomerTest {
	
	@Autowired
	private CustomerMapper customerMapper;
	
	/*@Before
    public void init() {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:applicationContext.xml"  
        ,"classpath:mybatis/mybatis-sqlserver.xml"});  
        //SpringBeanProxy.setApplicationContext(applicationContext);
    }*/
	
	
	/*@Before
	 public void setUp() throws Exception {
	     InputStream fis = null;
	     InputStream inputStream = null;
	     try {
	         //创建Properties对象
	 Properties prop = new Properties();
	 //创建输入流，指向配置文件,getResourceAsStream可以从classpath加载资源
	 fis= Resources.getResourceAsStream("classpath:properties/jdbc.properties");
	 //加载属性文件
	 prop.load(fis);
	 inputStream = Resources.getResourceAsStream("classpath:spring/applicationContext.xml");
	 //build的第二个参数对应mybatis.xml配置文件的<environment id="development">标签的id，
	 //其中后面两个参数可选，若第二个参数不写则默认为"development"
	 SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream,"development",prop);
	 SqlSession sqlSession = sqlSessionFactory.openSession();
	 //StuScoreDao.class与配置文件StuMapper的namespace对应
	     customerMapper = sqlSession.getMapper(CustomerMapper.class);
	 } catch (IOException e) {
	     // TODO Auto-generated catch block
	     e.printStackTrace();
	 }finally{
	     if(fis != null){
	         try {
	             fis.close();
	         } catch (IOException e) {
	             // TODO Auto-generated catch block
	         e.printStackTrace();
	     }
	 }
	 if(inputStream != null){
	     try {
	         inputStream.close();
	     } catch (IOException e) {
	         // TODO Auto-generated catch block
	                 e.printStackTrace();
	             }
	         }
	     }
	 }*/
	
	@Test
	public void test(){
		Customer s = customerMapper.selectByPrimaryKey(1L);
		System.out.println(s.getLoginName());
	}
}
