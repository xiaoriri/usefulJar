package plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;

import java.util.List;
import java.util.Map;

/**
 * 0、全局在插件配置属性里面使用：globalEnable = true 开启
 * 1、生成一个自定义的修改接口，使用参数：enableConditionUpdate 开启
 * 2、使用这个功能的时候需要特别小心，表结构高度相关
 */
public class ConditionUpdatePlugin extends PluginAdapter {


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
        String updateColumnValues = "updateColumnValues";
        //第二个函数参数的名称
        String updateConditions = "updateConditions";

        //一个查询节点
        XmlElement selectXmlElement = new XmlElement("update");
        //添加节点注释
        context.getCommentGenerator().addComment(selectXmlElement);

        //添加ID属性
        selectXmlElement.addAttribute(new Attribute("id","updateByCondition"));

        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("update\n").append("\t\t`").append(tableName).append("`\n");
        sqlBuilder.append("\t<set>\n")
                .append("\t\t").append("<if test=\"updateColumnValues neq null and updateColumnValues.keySet().size() gt 0\">\n")
                .append("\t\t\t").append("<foreach collection=\"updateColumnValues\" item=\"updateColumnValue\" index=\"columnName\">\n")
                .append("\t\t\t\t").append("${columnName} = #{updateColumnValue},\n")
                .append("\t\t\t").append("</foreach>\n")
                .append("\t\t").append("</if>\n")
                .append("\t").append("</set>\n")
                .append("\t").append("<where>\n")
                .append("\t\t").append("<if test=\"updateConditions neq null and updateConditions.keySet().size() gt 0\">\n")
                .append("\t\t\t").append("<foreach collection=\"updateConditions\" item=\"conditionValue\" index=\"conditionName\">\n")
                .append("\t\t\t\t").append("and ${conditionName} = #{conditionValue}\n")
                .append("\t\t\t").append("</foreach>\n")
                .append("\t\t").append("</if>\n")
                .append("\t").append("</where>");

        /*<update id="updateByCondition">
                    update
            `project_basic_info`
                    <set>
                <if test="updateColumnValues neq null and updateColumnValues.keySet().size() gt 0">
                    <foreach collection="updateColumnValues" item="updateColumnValue" index="columnName">
                    ${columnName} = #{updateColumnValue},
                    </foreach>
                </if>

            </set>
            <where>
                <if test="updateConditions neq null and updateConditions.keySet().size() gt 0">
                    <foreach collection="updateConditions" item="conditionValue" index="conditionName">
                    and ${conditionName} = #{conditionValue}
                    </foreach>
                </if>
            </where>
        </update>*/

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

//        int updateByCondition(@Param("updateColumnValues") Map<String,Object> updateColumnValues,@Param("updateConditions") Map<String,Object> updateConditions);

        //获取方法的名称
        String methodName = "updateByCondition";

        //创建这个方法
        Method method = new Method(methodName);

        //为这个方法加上文档注释信息
        method.addJavaDocLine("/**");
        method.addJavaDocLine("* 插件生成，请不要修改！");
        method.addJavaDocLine("* 1、当updateColumnValues为null或者size为0时将直接报错，所以必须要有字段更新。");
        method.addJavaDocLine("* 2、当updateConditions为null或者size为0时将更改所有的数据。");
        method.addJavaDocLine("* 3、当传入字段名称或者类型出错时会报错：BadSqlGrammarException等错误。");
        method.addJavaDocLine("* 4、updateConditions的所有的条件为and而不是or。");
        method.addJavaDocLine("*/");

        //设置方法的访问权限为public
        method.setVisibility(JavaVisibility.PUBLIC);

        //设置方法的返回值：int
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        //导入方法需要的类型
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.Map"));

        //第一个函数参数的名称
        String updateColumnValues = "updateColumnValues";
        //第二个函数参数的名称
        String updateConditions = "updateConditions";

        //添加两个函数入参
        method.addParameter(0,new Parameter(
                new FullyQualifiedJavaType("java.util.Map<java.lang.String,java.lang.Object>")
                ,updateColumnValues,"@Param(\"updateColumnValues\")"));

        method.addParameter(1,new Parameter(
                new FullyQualifiedJavaType("java.util.Map<java.lang.String,java.lang.Object>")
                ,updateConditions,"@Param(\"updateConditions\")"));

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

            final String tableEnable = introspectedTable.getTableConfigurationProperty("enableConditionUpdate");

            if(tableEnable != null){

                return Boolean.valueOf(tableEnable);
            }

            return true;
        }

        return false;
    }
}
