<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="http://blog.csdn.net/chenyufeng1991/article/details/78242369">乞力马扎罗的雪雪</a>
 </div> 
 <div>
  Converter是Spring3中引入的一项比较特殊的功能，其实就是一个转换器，可以把一种类型转换为另一种类型。尤其是在web项目中比较常见，可以对接口的入参做校验，把前端的入参的类型转换后为后端可以使用的类型。如常常用来做类型转换、字符串去空、日期格式化等。在Spring3之前进行类型转换都是使用PropertyEditor，使用PropertyEditor的setAsText()方法可以实现String转向特定的类型，但是它的最大的一个缺陷就是只支持String转为其他类型。
 </div> 
 <div></div> 
 <div>
  Spring中的三种类型转换接口分别为：
 </div> 
 <div></div> 
 <ul> 
  <li>Converter接口：使用最简单，但是不太灵活；</li> 
  <li>ConverterFactory接口：使用较复杂，稍微灵活一点；</li> 
  <li>GenericConverter接口：使用最复杂，也最灵活；</li> 
 </ul> 
 <div>
  由于枚举类型在开发中很常见，而前端无法直接传入一个Java中的枚举类型。该篇博客将会使用Converter来把一个前端传入的String转化为枚举，以方便使用。
 </div> 
 <div></div> 
 <ul> 
  <li>首先了解一下Converter接口</li> 
 </ul> 
 <div>
  Spring中Converter接口如下：
 </div> 
 <div></div> 
 <div> 
  <div> 
   <pre class="brush: java; gutter: true">package org.springframework.core.convert.converter;

public interface Converter&lt;S, T&gt; {
    T convert(S var1);
}</pre> 
  </div> 
 </div> 
 <div></div> 
 <div>
  即可以把S类型转化为T类型，并支持泛型。
 </div> 
 <div></div> 
 <ul> 
  <li>首先建立一个简单的对象类Person</li> 
 </ul> 
 <div>
  其中的一个字段性别GenderEnum使用了枚举。
 </div> 
 <div></div> 
 <div> 
  <pre class="brush: java; gutter: true">public class Person {

    private String name;
    private GenderEnum type;

    public Person() {
    }

    public Person(GenderEnum type, String name) {
        this.type = type;
        this.name = name;
    }

    public GenderEnum getType() {
        return type;
    }

    public void setType(GenderEnum type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}</pre> 
 </div> 
 <ul> 
  <li>需要转化的枚举类</li> 
 </ul> 
 <div>
  两个字段分别为male、female。现在的需求是前端分别传入“male”、“female”，后台就能转换为枚举中的male,female.
 </div> 
 <div> 
  <div></div> 
  <div> 
   <pre class="brush: java; gutter: true">public enum GenderEnum {
    male("male"),
    female("female");

    private final String value;

    GenderEnum(String v) {
        this.value = v;
    }

    public String toString() {
        return this.value;
    }

    public static GenderEnum get(int v) {
        String str = String.valueOf(v);
        return get(str);
    }

    public static GenderEnum get(String str) {
        for (GenderEnum e : values()) {
            if (e.toString().equals(str)) {
                return e;
            }
        }
        return null;
    }
}</pre> 
  </div> 
 </div> 
 <ul> 
  <li>Convert转换类</li> 
 </ul> 
 <div>
  在这个类中实现了Converter接口，并重写convert方法。
 </div> 
 <div> 
  <div></div> 
  <div> 
   <pre class="brush: java; gutter: true">final class StringToGenderEnumConverter implements Converter&lt;String, GenderEnum&gt; {

    @Override
    public GenderEnum convert(String source) {
        String value = source.trim();
        if ("".equals(value)) {
            return null;
        }
        return GenderEnum.get(Integer.parseInt(source));
    }
}</pre> 
  </div> 
 </div> 
 <ul> 
  <li>spring-mvc.xml配置</li> 
 </ul> 
 <div>
  在Spring配置文件中，配置conversionService。
 </div> 
 <div></div> 
 <div> 
  <pre class="brush: java; gutter: true">&lt;mvc:annotation-driven conversion-service="conversionService"/&gt;
&lt;bean id="conversionService" class="org.springframework.context.support.ConversionServiceFactoryBean"
    &lt;property name="converters"
        &lt;set&gt;
            &lt;bean class="com.chenyufeng.springmvc.common.converter.StringToGenderEnumConverter"/&gt;
        &lt;/set&gt;
    &lt;/property&gt;
&lt;/bean&gt;</pre> 
 </div> 
 <ul> 
  <li>Controller</li> 
 </ul> 
 <div>
  在Controller接口中，对于枚举字段，前端可以直接传入String类型的“male”、“female”，Converter可以自动进行转换。
 </div> 
 <div></div> 
 <div> 
  <pre class="brush: java; gutter: true">@Api(value = "converter", description = "自动转换", produces = MediaType.APPLICATION_JSON_VALUE)
@Controller
@RequestMapping("/converter")
public class ConverterController {

    @Autowired
    private ConverterService converterService;

    @RequestMapping(value = "/savePerson", method = RequestMethod.GET)
    @ResponseBody
    public String savePerson(
            @ApiParam(value = "type") @RequestParam GenderEnum type,
            @ApiParam(value = "ID") @RequestParam String id) {

        Person person = new Person(type, id);
        return converterService.savePerson(person);
    }
}</pre> 
 </div> 
 <div></div> 
 <div>
  以上案例上传至：
  <a href="https://github.com/chenyufeng1991/StartSpringMVC_Modules" target="_blank" class="external" rel="nofollow">https://github.com/chenyufeng1991/StartSpringMVC_Modules</a>中。
 </div> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>