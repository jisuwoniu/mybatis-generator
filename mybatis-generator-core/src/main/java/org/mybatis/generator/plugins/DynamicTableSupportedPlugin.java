/**
 * @ProjectName: 民用软件平台软件
 * @Copyright: 2012 HangZhou Hikvision System Technology Co., Ltd. All Right Reserved.
 * @address: http://www.hikvision.com
 * @date: 2013-12-13 下午2:39:36
 * @Description: 本内容仅限于杭州海康威视数字技术股份有限公司内部使用，禁止转发.
 */
package org.mybatis.generator.plugins;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 支持动态表生成的MyBabits-Generator Plugin，按照如下的思路进行设计：
 * <p>
 * 1. 生成的Mapper接口里面将Table作为参数传递命名为XXXMapperWithTableName；
 * <p>
 * 2. 生成的Mapper接口涉及到Table名称的注解，将原来的TableName替代为#tableName；
 * <p>
 * 3. 生成的Builder,所有方法加Table名称作为参数；
 * <p>
 * 4. 生成的Builder，原有的写死的Table名称，使用参数中的TableName进行替换;
 * <p>
 * 5. 生成一个干净的Mapper接口的TEmplate，用于Service的调用，命名为XXXTableTemplate
 * <p>
 * <p>
 * <b>使用方式：<b>
 * <p>
 * 
 * <pre>
 *    新建一个XXXMapper //实现接口里面去掉Mapper接口方法中的TAbleName，其他保持一致;
 *    XXXMapperDecroator包装表的生成策略，比如根据AppName生成等。
 *    Client端调用XXXMapperDecorator而不是XXXMapper.
 * </pre>
 * <p>
 * </p>
 * 
 * @author mingxu 2013-12-13 下午2:39:36
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2013-12-13
 * @modify by reason:{方法名}:{原因}
 */
public class DynamicTableSupportedPlugin implements Plugin {

    static final FullyQualifiedJavaType STR_QUALIFIED_TYPE = new FullyQualifiedJavaType("String");

    static final String TABLE_NAME_PARAM = "tableName";

    /**
     * 创建一个新的实例DynamicTableSupportedPlugin.
     */
    public DynamicTableSupportedPlugin() {
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * @see org.mybatis.generator.api.Plugin#validate(java.util.List)
     */
    @Override
    public boolean validate(List<String> warnings) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setContext(Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProperties(Properties properties) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub

    }

    /**
     * 不需要产生额外的Java文件
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        return null;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {

        List<GeneratedJavaFile> mapper = new ArrayList<GeneratedJavaFile>(1);
        for (GeneratedJavaFile f : introspectedTable.getGeneratedJavaFiles()) {
            if (f.getFileName().indexOf("Mapper") > -1) {
                CompilationUnit cu = f.getCompilationUnit();
                if (cu instanceof Interface) {
                    for (Method m : ((Interface) cu).getMethods()) {
                        MapperInterfaceHandler.processMapperMethod(m, introspectedTable);
                    }
                    
                    mapper.add(f);
                    mapper.add(new MapperDelegatorGenerator((Interface) cu, f).generteDelegate());
                }                
            } else if(f.getFileName().indexOf("Example") > -1
                    || f.getFileName().indexOf("SqlProvider") < 0){
                //添加一个TableName的属性&GET&SET方法
                CompilationUnit cu = f.getCompilationUnit();
                if(cu instanceof TopLevelClass) {
                    TopLevelClass c = (TopLevelClass) cu;
                    ExampleAndRecordClassHandler.appendTableNameField(c);
                }                
                mapper.add(f);                
            }
        }

        return mapper;
    }
    
    /**
     * 不需要产生额外的Java文件
     */
    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles() {
        return null;
    }

    /**
     * 不需要产生额外的Java文件
     */
    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable introspectedTable) {

        return null;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * countByExample方法，添加tableName参数
     */
    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * countByExample方法，添加tableName参数
     */
    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 1. 添加参数； 2. 修改注解
     * <p>
     * 
     * <pre>
     * @Delete({
     *   "delete from #{tableName}",
     *   "where id = #{id,jdbcType=INTEGER}" ---->
     *  })
     * </pre>
     */
    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;

    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapResultMapWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapBaseColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapBlobColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapResultMapWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean providerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean providerApplyWhereMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean providerCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return this.processParamsAndBodyLines(method, topLevelClass, introspectedTable);
    }

    @Override
    public boolean providerDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return this.processParamsAndBodyLines(method, topLevelClass, introspectedTable);
    }

    @Override
    public boolean providerInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return this.processParamsAndBodyLines(method, topLevelClass, introspectedTable);
    }

    @Override
    public boolean providerSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return this.processParamsAndBodyLines(method, topLevelClass, introspectedTable);
    }

    @Override
    public boolean providerSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return this.processParamsAndBodyLines(method, topLevelClass, introspectedTable);
    }

    @Override
    public boolean providerUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return this.processParamsAndBodyLines(method, topLevelClass, introspectedTable);
    }

    @Override
    public boolean providerUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return this.processParamsAndBodyLines(method, topLevelClass, introspectedTable);
    }

    @Override
    public boolean providerUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return this.processParamsAndBodyLines(method, topLevelClass, introspectedTable);
    }

    @Override
    public boolean providerUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return this.processParamsAndBodyLines(method, topLevelClass, introspectedTable);
    }

    private boolean processParamsAndBodyLines(Method method, TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
//        this.appendTableNameParameter(method);
        return this.replaceBodyLine(method, topLevelClass, introspectedTable);
    }

   

    private boolean replaceBodyLine(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<String> bodyLines = new ArrayList<String>(method.getBodyLines());
        method.getBodyLines().clear();
        
        String paramName = method.getParameters().get(0).getName();
        
        //3种情况获取TableName： 1）参数为XXExample ---getTableName; 2）参数为DO类 --> getTableName； 3）
        //     参数为Map<String, Object> parameter --> parameter.get("TARGET_TABLE_NAME")
        String replaceValue;
        if(method.getParameters().get(0).getType().getShortName().startsWith("Map<")) {
            replaceValue = String.format("(String) %s.get(\"%s\")", paramName, TABLE_NAME_PARAM);
        } else {
            replaceValue = String.format("%s.getTableName()", paramName);
        }
        
        //替换
        for (String line : bodyLines) {
            if (line.contains("FROM") || line.contains("DELETE_FROM") || line.contains("INSERT_INTO")
                    || line.contains("UPDATE")) {
                line = line.replaceAll(
                        String.format("\"%s\"", introspectedTable.getTableConfiguration().getTableName()),
                        replaceValue);
            }

            method.addBodyLine(line);
        }
        return true;
    }

    public static void generate() {
        String config = DynamicTableSupportedPlugin.class.getClassLoader().getResource("mybatis-generator.xml")
                .getFile();
        String[] arg = {
            "-configfile", config, "-overwrite"};
        ShellRunner.main(arg);
    }

    public static void main(String[] args) {
        generate();
    }
    

}
