package plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.config.TableConfiguration;

import java.util.List;
import java.util.Map;

/**
 * 0、全局在插件配置属性里面使用：globalEnable = true 开启
 * 1、生成一个自定义的查询接口，使用参数：enableConditionSelect 开启
 * 2、使用这个功能的时候需要特别小心，表结构高度相关
 * 3、需要开启 enableSelectByPrimaryKey，否则要自己生成
 */
public class ConditionSelectPlugin extends PluginAdapter {


    /**
     * 构建自定义的Mapper配置文件的SQL语句
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document,IntrospectedTable introspectedTable){

        if(!isPluginEnable(introspectedTable)){
            return true;
        }

        //获取表的名称
        String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();

        //第一个函数参数的名称
        String resultColumnNames = properties.getProperty("resultColumnNames","resultColumnNames");
        //第二个函数参数的名称
        String selectConditions = properties.getProperty("selectConditions","selectConditions");

        //一个查询节点
        XmlElement selectXmlElement = new XmlElement("select");
        //添加节点注释
        context.getCommentGenerator().addComment(selectXmlElement);

        //添加ID属性
        selectXmlElement.addAttribute(new Attribute("id",properties.getProperty("methodName","selectByCondition")));
        //添加resultMap属性
        selectXmlElement.addAttribute(new Attribute("resultType",Map.class.getName()));

        final TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();

        final boolean selectByPrimaryKeyStatementEnabled = tableConfiguration.isSelectByPrimaryKeyStatementEnabled();

        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("select\n");

        sqlBuilder.append("\t\t")
                .append("<if test=\"").append(resultColumnNames).append(" neq null and ").append(resultColumnNames).append(".size() gt 0").append("\">\n")
                .append("\t\t\t").append("<foreach collection=\"").append(resultColumnNames).append("\" separator=\",\" item=\"columnName\">\n")
                .append("\t\t\t\t").append("${columnName}\n")
                .append("\t\t\t").append("</foreach>\n")
                .append("\t\t").append("</if>\n")
                .append("\t\t").append("<if test=\"").append(resultColumnNames).append(" eq null or ").append(resultColumnNames).append(".size() eq 0").append("\">\n");

        if(selectByPrimaryKeyStatementEnabled){
            sqlBuilder.append("\t\t\t").append("<include refid=\"Base_Column_List\" />\n");
        }else {
            sqlBuilder.append("\t\t\t").append("*\n");
        }

        sqlBuilder.append("\t\t").append("</if>\n")
                .append("\tfrom\n")
                .append("\t\t`").append(tableName).append("`\n")
                .append("\t<where>\n")
                .append("\t\t").append("<if test=\"").append(selectConditions).append(" neq null and ").append(selectConditions).append(".keySet().size() gt 0").append("\">\n")
                .append("\t\t\t").append("<foreach collection=\"").append(selectConditions).append("\" item=\"conditionValue\" index=\"conditionName\">\n")
                .append("\t\t\t\t").append("and ${conditionName} = #{conditionValue}\n")
                .append("\t\t\t").append("</foreach>\n")
                .append("\t\t").append("</if>\n")
                .append("\t</where>");
        /*
        select
          <if test="resultColumnNames != null">
              <foreach collection="resultColumnNames" separator="," item="columnName">
                  ${columnName}
              </foreach>
          </if>
          <if test="resultColumnNames == null">
              *
          </if>
        from
          test_table
        <where>
            <if test="conditions != null">
                <foreach collection="conditions" item="conditionValue" index="conditionName">
                    and ${conditionName} = #{conditionValue}
                </foreach>
            </if>
        </where>
         */
        selectXmlElement.addElement(new TextElement(sqlBuilder.toString()));

        document.getRootElement().addElement(1,selectXmlElement);

        return true;
    }

    /**
     * 构建自定义的接口定义方法
     */
    @Override
    public boolean clientGenerated(Interface interfaze,TopLevelClass topLevelClass,IntrospectedTable introspectedTable){

        if(!isPluginEnable(introspectedTable)){
            return true;
        }
//        List<Map<String,Object>> selectByCondition(@Param("resultColumnNames") List<String> resultColumnNames,@Param("conditions")Map<String,Object> conditions);

        //获取方法的名称
        String methodName = properties.getProperty("methodName","selectByCondition");

        //创建这个方法
        Method method = new Method(methodName);

        //为这个方法加上文档注释信息
        method.addJavaDocLine("/**");
        method.addJavaDocLine("* 插件生成，请不要修改！");
        method.addJavaDocLine("* 1、当resultColumnNames为null或者size为0时将查询所有的字段。");
        method.addJavaDocLine("* 2、当selectConditions为null或者size为0时将没有查询条件，所有的条件是and关系，不是or关系。");
        method.addJavaDocLine("* 3、当传入字段名称或者类型出错时会报错：BadSqlGrammarException等错误。");
        method.addJavaDocLine("* 4、该方法禁止在非基础服务层调用，也禁止对其进行简单包装后提供给其他服务调用。");
        method.addJavaDocLine("*/");

        //设置方法的访问权限为public
        method.setVisibility(JavaVisibility.PUBLIC);

        //设置方法的返回值：List<Map<String,Object>>
        method.setReturnType(new FullyQualifiedJavaType("java.util.List<java.util.Map<java.lang.String,java.lang.Object>>"));

        //导入方法需要的两个类型
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.List"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.Map"));

        //第一个函数参数的名称
        String resultColumnNames = properties.getProperty("resultColumnNames","resultColumnNames");
        //第二个函数参数的名称
        String selectConditions = properties.getProperty("selectConditions","selectConditions");

        //添加两个函数入参
        method.addParameter(0,new Parameter(
                new FullyQualifiedJavaType("java.util.List<java.lang.String>")
                ,resultColumnNames,"@Param(\"" + resultColumnNames + "\")"));

        method.addParameter(1,new Parameter(
                new FullyQualifiedJavaType("java.util.Map<java.lang.String,java.lang.Object>")
                ,selectConditions,"@Param(\"" + selectConditions + "\")"));

        //将入参的注解导入进来
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        //将这个方法添加到接口里面去
        interfaze.addMethod(method);

        return true;
    }

    @Override
    public boolean validate(List<String> warnings){
        return true;
    }

    /**
     * 判断一个表是否开启这个插件
     */
    private boolean isPluginEnable(IntrospectedTable introspectedTable){

        final String globalEnable = properties.getProperty("globalEnable");

        if(globalEnable == null || Boolean.valueOf(globalEnable)){

            final String tableEnable = introspectedTable.getTableConfigurationProperty("enableConditionSelect");

            if(tableEnable != null){

                return Boolean.valueOf(tableEnable);
            }

            return true;
        }

        return false;
    }

}
