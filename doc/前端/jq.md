### select

```js

var sel = $("#wjId");
var sel = $("select[name='" + selectName + "']");		//也可以使用这种方式获取select元素
sel.change(function () {								//选项改变事件
	var wjName = $("#wjId").find("option:selected").text();	//获取当前选择的文本
    sel.append('<option value="' + data[i]["pkId"] + '">' + data[i]["questionContent"] + '</option>');		//动态添加select选项
});

sel.select2();		//动态添加选项后，调用此方法 (../plugin/select2/js/select2.js)。

sel.empty();											//清空选项
sel.append('<option id="removeOption" value=""> </option>');	//这是一个空白选项

var templateId = $("#templateId").val();	//选中的值

//动态生成select 默认选中第一个
baseAjax2("../standardContractTemplate/getTemplateList.do",{},function(data){
    var sobj = $("#templateId");
    sobj.empty();
    // sobj.append('<option id="removeOption" value="">请选择</option>'); //添加请选择
    for (var i = 0; i < data.length; i++) {
        if(i==0){
            sobj.append('<option value="' + data[i]["PK_ID"] + '" selected >' + data[i]["NAME"] + '</option>');
            //queryTemplateConfList();
        }else{
            sobj.append('<option value="' + data[i]["PK_ID"] + '">' + data[i]["NAME"] + '</option>');
        }
    }
    sobj.select2();
});

```

### input

```js
$("#id").val("");			//清空input值
$("#id").val("aaa");		//设置值

//获取单选按钮选中的值
var loginType = $("input[name='loginType']:checked").val();
```

### form

```js
baseAjax("../standardContractTemplateConf/get.do",
         $("#questionForm").serialize(),		//获取表单中属性和值
         function(msg){
            if(msg.success){
                $('#context').setform(msg.result);	//填充表单数据
            }
});

```

### 数组

```js
var arrName = [];					//定义数组
var data = {name:"name", value: 0};	//定义集合 echart使用
arrName.push(data);					//添加数据
```

### class

```js
$("#tjTab .sy-left").removeClass("current");  		//移除class
temp.addClass("current");							//添加class
$(this).is('.current')								//是否包含class
```

### 控制\<div/>显隐

```js
//css方式
$("#letterDataEcharts,#visitDataEcharts,#zxDataEcharts").css('display','none');
$("#letterDataEcharts").css('display','block');
//函数方式
$("#letterDataEcharts,#visitDataEcharts,#zxDataEcharts").hide();
$("#letterDataEcharts").show();
```

### span

```js
<span id="exportTotal" style="margin-right: auto;margin-bottom: -10px;"></span>
$("#exportTotal").html("导出数据总计"+ count +"条")
```

### 获取url参数

```java
$.getUrlParam();
//包含中文：编码/解码
encodeURI();
decodeURI();
```

### 单选按钮

```js
$("#status1").attr("checked","checked")

```

### 所选元素后添加内容

```
$(".filedRow").after('<tr class="tableColumnName filedTr">');
```

