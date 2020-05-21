package plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

public class JpaEntityPlugin extends PluginAdapter {

	/**
	 * 为JpaEntity的类级别增加注解的方法
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

		// 清空里面所有的方法，所有的方法由lombok自动生成，保持文件的干爽和清晰
		topLevelClass.getMethods().clear();

		// 导入需要导入的类
		topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");
		topLevelClass.addImportedType("lombok.*");
//		topLevelClass.addImportedType("javax.persistence.*");

		final List<String> annotations = topLevelClass.getAnnotations();

		// 添加lombok的相关注解
		annotations.add("@Data");
//		annotations.add("@Builder");
//		annotations.add("@NoArgsConstructor");
//		annotations.add("@AllArgsConstructor");
//		
//	      //添加JPA的相关注解
//        annotations.add("@Entity");
//
//        final String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
//
//        annotations.add("@Table(name = \"" + tableName + "\")");

	

		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * 为JpaEntity的字段添加Jpa，注意，必须要有一个字段为id的自增主键
	 */
	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {

		final String columnName = introspectedColumn.getRemarks();

		final String name = introspectedColumn.getActualColumnName();
		
		final List<String> annotations = field.getAnnotations();

		// 添加Jpa的相关注解
		annotations.add("@ApiModelProperty(value = \"" + columnName + "\", required = true)");

		return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
	}

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

}
