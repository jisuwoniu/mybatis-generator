/**
 * @ProjectName: 民用软件平台软件
 * @Copyright: 2012 HangZhou Hikvision System Technology Co., Ltd. All Right Reserved.
 * @address: http://www.hikvision.com
 * @date: 2013-12-17 上午8:48:29
 * @Description: 本内容仅限于杭州海康威视数字技术股份有限公司内部使用，禁止转发.
 */
/**
 * <p></p>
 * @author mingxu 2013-12-17 上午8:48:29
 * @version V1.0   
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2013-12-17
 * @modify by reason:{方法名}:{原因}
 */
package org.mybatis.generator.plugins;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.ObjectFactory;

import java.util.ArrayList;
import java.util.List;


enum MapperMethodEnum {
    COUNT_BY_EXAMPLE("countByExample"),
    DELETE_BY_EXAMPLE("deleteByExample"), 
    DELETE_BY_PRIMARY_KEY("deleteByPrimaryKey"), 
    INSERT("insert"), 
    INSERT_SELECTIVE("insertSelective"), 
    SELECT_BY_EXAMPLE("selectByExample"),
    SELECT_BY_PRIMARY_KEY("selectByPrimaryKey"), 
    UPDATE_BY_EXAMPLE_SELECTIVE("updateByExampleSelective"),
    UPDATE_BY_EXAMPLE("updateByExample"), 
    UPDATE_BY_PRIMARY_KEY_SELECTIVE("updateByPrimaryKeySelective"),
    UPDATE_BY_PRIMARY_KEY("updateByPrimaryKey"),
    NOT_FOUND_METHOD("notFoundMethod");

    private String methodName;

    MapperMethodEnum(String methodName) {
        this.methodName = methodName;
    }

    static MapperMethodEnum parseValue(String method) {
        for (MapperMethodEnum v : MapperMethodEnum.values()) {
            if (v.methodName.equals(method)) {
                return v;
            }
        }

        return NOT_FOUND_METHOD;
    }
}

/**
 * Mapper代理类的产生器，对于其中的Context，实际上更好的实现方式是使用一个模板，可惜的是Generator的Class并不支持。
 * 
 * <p></p>
 * @author mingxu 2013-12-17 上午8:50:49
 * @version V1.0   
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2013-12-17
 * @modify by reason:{方法名}:{原因}
 */
class MapperDelegatorGenerator {
    private static final String[] CLASS_DOC_LINES = {
        "/**",
        "* Auto generate MapperDelegator class, you can optional user it <p>",
        "* usage: YouService --(Reference)-> XXXMapperDelegator --> <p>",
        "* YourTableGenStragy extends XXXMapperDelegator and implement the abstract methods<p>",
        "* ",
        "* TODO 实际上使用模板来代替Object类型是更好的方式，遗憾的是Mybaits Generator并不支持带有模板的类的生成... <p>",
        "* <p>",
        "*/"
    };
    
    private static final String[] ABS_METHOD_DOC_LINES = {
        "/**",
        "* Decide the table name accroding to specified context <p>",
        "* @param context context of your app, contains the data which can deference the table name",
        "* @return the table name according to context values",
        "*/"
    };
    
    private static String CONTEXT_PARAM_NAME = "context";
    private static String CONTEXT_TYPE = "Object";
    
    private Interface mapper;

    private String attrName;

    private GeneratedJavaFile prototype;
    
    MapperDelegatorGenerator(Interface mapper, GeneratedJavaFile prototype) {
        this.mapper = mapper;
        attrName = mapper.getType().getShortName();
        attrName = Character.toLowerCase(attrName.charAt(0)) + attrName.substring(1);
        this.prototype = prototype;
    }

    /**
     * 创建Delegagor的方法，会产生一个Java文件的描述
     * 
     * <p></p>
     * @author mingxu 2013-12-17 上午8:55:18
     * @return
     */
    public GeneratedJavaFile generteDelegate() {
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(mapper.getType().getFullyQualifiedName()
                + "Delegator");
        TopLevelClass tpc = new TopLevelClass(type);
        tpc.setAbstract(true);
        tpc.setVisibility(JavaVisibility.PUBLIC);
        for(String line : CLASS_DOC_LINES) {
            tpc.addJavaDocLine(line);
        }
        
        //imports: 将接口里面的除了Ibatis的引用之外的Import都加进来(实际上是java.util.List, xxxDO, XXXExample)
        tpc.addImportedType("javax.annotation.Resource");
        for(FullyQualifiedJavaType importType : mapper.getImportedTypes()) {
            if(importType.getPackageName().indexOf("org.apache") < 0) {
                tpc.addImportedType(importType);
            }
        }
        
        
        //Mapper fields
        this.generateFileds(tpc);
        
        for(Method m : this.mapper.getMethods()) {
            this.generateMethod(tpc, m);
        }
        
        //Abstract method(template method)
        this.generateAbstractMethod(tpc);
        
        GeneratedJavaFile file = new GeneratedJavaFile(tpc, this.prototype.getTargetProject(),
                this.prototype.getFileEncoding(),
                ObjectFactory.createJavaFormatter(new Context(null)));
        
        return file;
    }
    
    private void generateFileds(TopLevelClass tpc) {
        Field f = new Field(this.attrName, this.mapper.getType());
        f.setVisibility(JavaVisibility.PRIVATE);
        f.addAnnotation("@Resource");
        
        tpc.addField(f);
    }

    /**
     * Copy Interface里面的所有方法，但是将tableName参数修改为T context(模板里面的参数)
     * 
     * <p></p>
     * @author mingxu 2013-12-17 上午9:01:31
     * @param tpc
     * @param m
     */
    private void generateMethod(TopLevelClass tpc, Method m) {
        Method t = new Method(m.getName());

        // 生成方法声明
        t.setConstructor(false);
        t.setFinal(false);
        t.setNative(false);
        t.setReturnType(m.getReturnType());
        t.setStatic(false);
        t.setSynchronized(false);
        t.setVisibility(JavaVisibility.PUBLIC);
        for (Parameter p : m.getParameters()) {
            Parameter tp = new Parameter(p.getType(), p.getName());
            if (!DynamicTableSupportedPlugin.TABLE_NAME_PARAM.equals(p.getName())) {
                t.addParameter(tp);
            }
        }
        
        //方法上下文的参数
        t.addParameter(new Parameter(new FullyQualifiedJavaType(CONTEXT_TYPE), CONTEXT_PARAM_NAME));
        
        this.generateMethodBodyLine(t, m);

        tpc.addMethod(t);
    }

    private void generateMethodBodyLine(Method target, Method interfaceMethod) {
        switch(MapperMethodEnum.parseValue(target.getName())) {
            case SELECT_BY_PRIMARY_KEY:
            case DELETE_BY_PRIMARY_KEY:
            case UPDATE_BY_EXAMPLE_SELECTIVE:   
            case UPDATE_BY_EXAMPLE:
                this.appendTableNameToParamBodyLine(target, interfaceMethod);
                break;
            case INSERT:
            case COUNT_BY_EXAMPLE:
            case DELETE_BY_EXAMPLE:
            case SELECT_BY_EXAMPLE:
            case INSERT_SELECTIVE:  
            case UPDATE_BY_PRIMARY_KEY:
            case UPDATE_BY_PRIMARY_KEY_SELECTIVE:
                this.appendTableNameToBeanBodyLine(target, interfaceMethod);
                break;                
            case NOT_FOUND_METHOD:
            default:
        }    
    }
    
    private void appendTableNameToBeanBodyLine(Method target, Method interfaceMethod){
        Parameter first = target.getParameters().get(0);
        target.addBodyLine(
                String.format("%s.setTableName(this.generteTableName(%s));", first.getName(), CONTEXT_PARAM_NAME));
        
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("return this.%s.%s(", this.attrName, interfaceMethod.getName()));
        
        List<Parameter> ps = target.getParameters();
        buf.append(ps.get(0).getName());
        for(int i = 1; i < ps.size() - 1; i++) {
            buf.append(',');
            buf.append(ps.get(i).getName());
        }
        buf.append(");");
        
        target.addBodyLine(buf.toString());
    }
    
    private void appendTableNameToParamBodyLine(Method target, Method interfaceMethod) {
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("return this.%s.%s(", this.attrName, interfaceMethod.getName()));
        for(Parameter p : target.getParameters()) {
            if(!p.getName().equals(CONTEXT_PARAM_NAME)) {
                buf.append(p.getName());
                buf.append(',');
            }
        }
        
        buf.append(String.format("this.generteTableName(%s));", CONTEXT_PARAM_NAME));
        target.addBodyLine(buf.toString());
    }
    
    private void generateAbstractMethod(TopLevelClass tpc) {
        Method m = new Method("generteTableName");
        m.setConstructor(false);
        m.setFinal(false);
        m.setNative(false);
        m.setReturnType(new FullyQualifiedJavaType("String"));
        m.setVisibility(JavaVisibility.PROTECTED);
        m.addParameter(new Parameter(new FullyQualifiedJavaType(CONTEXT_TYPE), CONTEXT_PARAM_NAME));
        
        for(String line : ABS_METHOD_DOC_LINES) {
            m.addJavaDocLine(line);
        }
        
        tpc.addMethod(m);
    }
}

/**
 * 处理Mapper接口的类
 * 
 * <p></p>
 * @author mingxu 2013-12-17 下午2:48:54
 * @version V1.0   
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2013-12-17
 * @modify by reason:{方法名}:{原因}
 */
class MapperInterfaceHandler {
    /**
     * 处理Mapper类的方法，对于调用Provider进行处理的地方，不做任何处理，直接通过注解执行的地方
     * 添加TableName作为参数
     * 
     * <p></p>
     * @author mingxu 2013-12-17 下午2:38:21
     * @param m
     * @param introspectedTable
     */
    static void processMapperMethod(Method m, IntrospectedTable introspectedTable) {
        switch (MapperMethodEnum.parseValue(m.getName())) {
            case SELECT_BY_PRIMARY_KEY:
            case DELETE_BY_PRIMARY_KEY:            
                appendTableNameParameterWithAnnotation(m);
                replaceMethodAnotation(m, introspectedTable);
                break;
            case UPDATE_BY_EXAMPLE_SELECTIVE:
            case UPDATE_BY_EXAMPLE:
                appendTableNameParameterWithAnnotation(m);
                break;
                
            case INSERT:
            case UPDATE_BY_PRIMARY_KEY:
                replaceMethodAnotation(m, introspectedTable);
                break;
            case COUNT_BY_EXAMPLE:
            case DELETE_BY_EXAMPLE:
            case INSERT_SELECTIVE:
            case SELECT_BY_EXAMPLE:
            case UPDATE_BY_PRIMARY_KEY_SELECTIVE:
            case NOT_FOUND_METHOD:
            default:

        }

    }
    
    private static void appendTableNameParameterWithAnnotation(Method method) {
        Parameter p = new Parameter(DynamicTableSupportedPlugin.STR_QUALIFIED_TYPE,
                DynamicTableSupportedPlugin.TABLE_NAME_PARAM);
        p.addAnnotation(String.format("@Param(\"%s\")", DynamicTableSupportedPlugin.TABLE_NAME_PARAM));
        method.addParameter(p);
        
        for(Parameter i : method.getParameters()) {
            if(i.getAnnotations().size() < 1) {
                i.addAnnotation(String.format("@Param(\"%s\")", i.getName()));
            }
        }
    }
    
     private static void replaceMethodAnotation(Method method, IntrospectedTable introspectedTable) {
        List<String> annotations = method.getAnnotations();
        List<String> target = new ArrayList<String>(annotations.size());
        boolean trigger = false;
        String prefix = "";
        
//        MarcoExecutor marco = new MarcoExecutor(introspectedTable.getTableConfiguration().getTableName(),
//                " #{tableName} ");
        for (String a : annotations) {

            if (a.indexOf("@Insert") > -1) {
                prefix = "insert into ";
                trigger = true;
            } else if (a.indexOf("@Update") > -1) {
                prefix = "update ";
                trigger = true;
            } else if (a.indexOf("@Delete") > -1 || a.indexOf("@Select") > -1) {
                prefix = "from ";
                trigger = true;
            }

            if (trigger) {
                a = a.replaceAll(prefix + introspectedTable.getTableConfiguration().getTableName(),
                        String.format(" %s \\${%s} ", prefix, DynamicTableSupportedPlugin.TABLE_NAME_PARAM));
            }

            target.add(a);
        }

        annotations.clear();
        annotations.addAll(target);
    }
    
    private static boolean appendTableNameParameter(Method method) {
        method.addParameter(new Parameter(DynamicTableSupportedPlugin.STR_QUALIFIED_TYPE, DynamicTableSupportedPlugin.TABLE_NAME_PARAM));
        return true;
    }
}

/**
 * 处理Example和Record(DO)的类，为其增加tableName的属性以及Set/Get方法
 * 
 * <p></p>
 * @author mingxu 2013-12-17 下午2:49:24
 * @version V1.0   
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2013-12-17
 * @modify by reason:{方法名}:{原因}
 */
class ExampleAndRecordClassHandler {
    static void appendTableNameField(TopLevelClass c) {
        Field tableNameField = new Field(DynamicTableSupportedPlugin.TABLE_NAME_PARAM, DynamicTableSupportedPlugin.STR_QUALIFIED_TYPE);
        tableNameField.setVisibility(JavaVisibility.PRIVATE);
        c.getFields().add(tableNameField);
        
        Method get = new Method();
        get.setName("getTableName");
        get.setVisibility(JavaVisibility.PUBLIC);
        get.addBodyLine("return this.tableName;");
        get.setReturnType(DynamicTableSupportedPlugin.STR_QUALIFIED_TYPE);
        
        Method set = new Method();
        set.setName("setTableName");
        set.setVisibility(JavaVisibility.PUBLIC);
        set.addBodyLine("this.tableName = tableName;");
        set.addParameter(new Parameter(DynamicTableSupportedPlugin.STR_QUALIFIED_TYPE, DynamicTableSupportedPlugin.TABLE_NAME_PARAM));
        
        c.addMethod(get);
        c.addMethod(set);
    }
}