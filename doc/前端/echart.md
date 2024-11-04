#### 通用部分

```js
var option = {
    title: {	//标题通用
        text: '名称',      //主标题
        textStyle:{
            color:'#0DB9F2',        //颜色
            fontStyle:'normal',     //风格
            fontWeight:'normal',    //粗细
            fontFamily:'Microsoft yahei',   //字体
            fontSize:14,     //大小
            align:'center'   //水平对齐
        },
        subtext:'副标题',      //副标题
        subtextStyle:{          //对应样式
            color:'#F27CDE',
            fontSize:14
        }
    },
    tooltip: {
        trigger: 'item',
        //{a}:series的name {b}:统计的样本 {c}:数量 {d}:百分比  <br/>：换行
        formatter: "{a} <br/>{b} : {c} ({d}%)"
    },
    //省略其他
}

```

#### 饼图

```js
//echart2.0
series: [
    {
        name: quest,
        type: 'pie',
        radius: '55%',		//饼图大小
        itemStyle: {        //每一个条目样式
            normal: {
                label: {	// 3.0以后不这么干了
                    show: true,
                    position: 'outside',	//inside
                    formatter: "{b} : {c} ({d}%)"	//a
                }
            }
        },
        center: ['50%', '60%'],		//距离左上角原点位置，左右距离
        data: arrData
    }
]
```

#### 柱状图

```js
var option = {
    grid:{		//设置整个图表与边框距离
        x:25,	//距离左边框
        x2:25,	//距离右边框
        y:150,	//距离顶边框
        y2:150,	//距离底边框
        width:150，	//宽
        height:150	//高
    },
}

//每一个柱
item= {
    name:name[j],
    type: 'bar',
    data:  arrData[j],
    markLine:{
        data:[{type:'average',name:'平均值'}],
        barWidth:30
    },
    itemStyle:{
        normal:{
            label:{
                show:true,  //开启显示
                position:'top', //在上方显示
                textStyle:{
                    color:'black',
                    fontSize:16
                },
                //柱状图上面增加百分比显示
                formatter:function (param) {
                    if(param.value){	//存在值则加，没有就为空
                        return param.value + '%';	
                    }else {
                        return '';
                    }

                }
            }
        }
    }
};

```

