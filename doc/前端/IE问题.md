解决IE9的json串被当文件提示的兼容性问题：把http的响应contentType改为 "text/plain",则firefox和IE都能正常响应



#### ajaxFileUpload上传文件进行查询并返回结果出错

直接使用application/json响应头返回JSON时，IE11不能调用此函数，需将响应头设置为text/html，将返回对象转为JSON字符串，使用write.print()写入字符串。

```
response.setContentType("text/html;charset=UTF-8");

String json = JSON.toJSONString(result);
response.getWriter().print(json);
```



#### IE9 JSON.stringify()

```

```

