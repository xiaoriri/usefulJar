package plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;

import java.util.List;

/**
 * 0、全局在插件配置属性里面使用：globalEnable = true 开启
 * 1、生成一个自定义的删除接口，单表使用参数：enableConditionDelete 开启
 * 2、使用这个功能的时候需要特别小心，表结构高度相关
 */
public class ConditionDeletePlugin extends PluginAdapter {


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
        String deleteConditions = "deleteConditions";

        //一个查询节点
        XmlElement selectXmlElement = new XmlElement("delete");
        //添加节点注释
        context.getCommentGenerator().addComment(selectXmlElement);

        //添加ID属性
        selectXmlElement.addAttribute(new Attribute("id","deleteByCondition"));

        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("delete from\n").append("\t\t`").append(tableName).append("`\n");
        sqlBuilder.append("\t").append("<where>\n")
                .append("\t\t").append("<if test=\"deleteConditions neq null and deleteConditions.keySet().size() gt 0\">\n")
                .append("\t\t\t").append("<foreach collection=\"deleteConditions\" item=\"conditionValue\" index=\"conditionName\">\n")
                .append("\t\t\t\t").append("and ${conditionName} = #{conditionValue}\n")
                .append("\t\t\t").append("</foreach>\n")
                .append("\t\t").append("</if>\n")
                .append("\t").append("</where>");

        /*<delete id="deleteByCondition">
                    delete from
            `project_basic_info`
                    <where>
                <if test="deleteConditions neq null and deleteConditions.keySet().size() gt 0">
                    <foreach collection="deleteConditions" item="conditionValue" index="conditionName">
                    and ${conditionName} = #{conditionValue}
                    </foreach>
                </if>
            </where>
        </delete>*/

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

//        int deleteByCondition(@Param("deleteConditions") Map<String,Object> deleteConditions);

        //获取方法的名称
        String methodName = "deleteByCondition";

        //创建这个方法
        Method method = new Method(methodName);

        //为这个方法加上文档注释信息
        method.addJavaDocLine("/**");
        method.addJavaDocLine("* 插件生成，请不要修改！");
        method.addJavaDocLine("* 1、当deleteConditions为null或者size为0时将删除所有的数据！！！！。");
        method.addJavaDocLine("* 2、当传入字段名称或者类型出错时会报错：BadSqlGrammarException等错误。");
        method.addJavaDocLine("* 3、deleteConditions的所有的条件为and而不是or。");
        method.addJavaDocLine("*/");

        //设置方法的访问权限为public
        method.setVisibility(JavaVisibility.PUBLIC);

        //设置方法的返回值：int
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        //导入方法需要的类型
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.Map"));

        //第一个函数参数的名称
        String deleteConditions = "deleteConditions";

        //添加函数入参
        method.addParameter(0,new Parameter(
                new FullyQualifiedJavaType("java.util.Map<java.lang.String,java.lang.Object>")
                ,deleteConditions,"@Param(\"deleteConditions\")"));

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

            final String tableEnable = introspectedTable.getTableConfigurationProperty("enableConditionDelete");

            if(tableEnable != null){
                return Boolean.valueOf(tableEnable);
            }

            return true;
        }

        return false;
    }
}
