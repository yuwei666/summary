验证数组/文本/日期

```html
<tr>
    <td class="tableColumnName">
        <span>值班日期<i class="red">*</i></span>
    </td>
    <td colspan="1">
        <input id="zbTime" name="zbTime" type="text" autocomplete="off" class="swmsTime"  required="required"/>
    </td>
    <td class="tableColumnName">
        <span>是否已值班登记</span>
    </td>
    <td colspan="1">
        <input id="isCheckinStatus" name="isCheckinStatus" type="text" readonly/>
        <input id="isCheckin" name="isCheckin" type="hidden"/>
    </td>
</tr>
```



#### 关于引入js

一个html引入多个js后，js中的方法可以互相调用，避免所有业务写在同一个js中，过长且不好管理