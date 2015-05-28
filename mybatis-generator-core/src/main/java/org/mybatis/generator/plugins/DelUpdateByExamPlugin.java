package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * 去掉生成的DAO接口中的updateByExample | updateByPrimaryKey，只保留其中的
 * updateByExampleSelective | udpateByPrimaryKeySelective两个接口，以避免开发人员误用。
 * 
 * <pre>
 *     使用方式：配置如下的Plugin即可自动过滤掉。
 *   	type="com.hikvision.shipin7.mybatis.DelUpdateByExamPlugin"
 * </pre>
 * 
 * 备注：只是在SQLMap和Mapper接口生成的时候去掉了，但是SQLProvider生成类里面仍然是存在的，因此
 * 使用者可以根据自身的情况进行必要的扩展（在Mapper的扩展里面使用SQLPrvodier的updateByPrimaryKey & updateByExample)
 * 
 * @author mingxu
 *
 */
public class DelUpdateByExamPlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> warnings) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

	public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(
			Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return false;
	}

	public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(
			Method method, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		return false;
	}

	public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(
			Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		return false;
	}

	public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(
			Method method, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {

		return false;
	}

	public static void generate() {
		String config = DelUpdateByExamPlugin.class.getClassLoader()
				.getResource("mybatis-generator.xml").getFile();
		String[] arg = {};
		AggregateRunner.main(arg);
	}

	public static void main(String[] args) {
		generate();
	}

}
