package plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;
import java.util.Properties;

/**
 * 0、全局在插件配置属性里面使用：globalEnable = true 开启
 * 1、实现为Mapper文件添加指定注解的功能
 * 2、单表开启参数：enableMapperAnnotationPlugin = true
 * 3、单表注解的分隔符，配置参数为：annotationSeparator，默认为：,
 * 4、单表注解类，使用配置：annotationClasses
 * 5、全局分隔符使用：globalAnnotationSeparator 配置
 * 6、全局注解使用：globalAnnotationClasses 配置
 */
public class MapperAnnotationPlugin extends PluginAdapter {

    @Override
    public boolean clientGenerated(Interface interfaze,TopLevelClass topLevelClass,IntrospectedTable introspectedTable){

        if(!isPluginEnable(introspectedTable)){
            return true;
        }

        final Properties tableProperties = getTableProperties(introspectedTable);

        //获取分隔符，默认为：,
        String separator = tableProperties.getProperty("annotationSeparator");

        if(separator == null){

            //获取全局分隔符
            separator = properties.getProperty("globalAnnotationSeparator",",");
        }

        String annotationClass = null;

        //获取需要添加的注解
        String tableAnnotationClass = tableProperties.getProperty("annotationClasses");

        if(tableAnnotationClass!=null){
            annotationClass = tableAnnotationClass;
        }

        //全局配置注解
        String globalAnnotationClass = properties.getProperty("globalAnnotationClasses");

        if(globalAnnotationClass != null){

            if(tableAnnotationClass == null){

                annotationClass = globalAnnotationClass;
            }else {

                annotationClass = tableAnnotationClass + separator + globalAnnotationClass;
            }
        }

        if(annotationClass != null){

            String[] annotationClazz = annotationClass.split(separator);

            if(annotationClazz.length > 0){

                for(String clazz : annotationClazz){

                    //导入注解
                    interfaze.addImportedType(new FullyQualifiedJavaType(clazz));
                    //添加注解
                    interfaze.addAnnotation("@" + clazz.substring(clazz.lastIndexOf(".")+1));
                }
            }
        }

        return true;
    }

    @Override
    public boolean validate(List<String> warnings){

        return true;
    }

    /**
     * 获取表的配置数据
     */
    private Properties getTableProperties(IntrospectedTable introspectedTable){

        return introspectedTable.getTableConfiguration().getProperties();
    }

    /**
     * 判断一个表是否开启这个插件
     */
    private boolean isPluginEnable(IntrospectedTable introspectedTable){

        final String globalEnable = properties.getProperty("globalEnable");

        if(globalEnable == null || Boolean.valueOf(globalEnable)){

            final String tableEnable = introspectedTable.getTableConfigurationProperty("enableMapperAnnotation");

            if(tableEnable != null){

                return Boolean.valueOf(tableEnable);
            }

            return true;
        }

        return false;
    }
}