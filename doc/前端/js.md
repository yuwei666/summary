```js
JSON.stringigy();
JSON.parse();
```



### this

```js
//在？？？？，this指向 window
this == window
//在点击事件中，this指向点击事件所在的标签<a class="export">导出</a>
this == a.export
```

### CKEDITOR

这是初始化了一个工具栏展开的CKEDITOR

```html
<script src="../plugin/ckeditor/ckeditor.js"></script>
...
<td colspan="3" >
    <textarea rows="24" id="dutyRegionDisplay" name="dutyRegionDisplay" class="ckeditor" required="required" valititle="该项为必填项"></textarea>
    <script type="text/javascript">
        CKEDITOR.replace("dutyRegionDisplay",{
            filebrowserImageUploadUrl: '../help/upload.do',
            toolbarStartupExpanded:true
        });</script>
    <div class="tag-position" id="drdDiv">
        <span class="tag-icon"></span>
        <div class="tag-content" style="color:red;float:left;">该项为必填项!</span></div>
</div>
</td>
```

**控制读写**

这是最重要的，等ckeditor初始化完成再设置显示内容，第二个为是否为阅读模式

```js
function waitCKEditorReady(data,isRead){
    var  ckEditor=CKEDITOR.instances.dutyRegionDisplay;
    if(ckEditor.status=='ready'){
        if(data !== null && data != undefined && data != ""){
            ckEditor.setData(data);
        }
        if(isRead) {
            $(".cke_bottom").css("display","none");
            $(".cke_top").css("display","none");
            ckEditor.setReadOnly(true);
        }else{
            $(".cke_bottom").css("display","block");
            $(".cke_top").css("display","block");
            ckEditor.setReadOnly(false);
        }
    }else{
        setTimeout(function (){
            waitCKEditorReady(data,isRead);
        },20);
    }
}
```



#### 下载

```js
window.location.href="../attachment/download2.do?attId="+array[0].attId;
```

