<div align=center>
  <img width="340" src="https://github.com/CyrilFeng/karma/blob/main/src/main/resources/static/img/1280X1280.PNG" />
</div>


<div align="center">
  <a href="javascript:;"><img src="https://img.shields.io/appveyor/build/gruntjs/grunt?label=%E6%9E%84%E5%BB%BA" /></a>
  <a href="javascript:;"><img src="https://img.shields.io/appveyor/build/gruntjs/grunt?label=%E6%B5%8B%E8%AF%95" /></a>
  <a href="javascript:;"><img src="https://img.shields.io/appveyor/build/gruntjs/grunt?label=%E6%96%87%E6%A1%A3" /></a>
  <a href="javascript:;"><img src="https://img.shields.io/badge/%E5%BC%80%E6%BA%90%E5%8D%8F%E8%AE%AE-Apache-brightgreen" alt="License"></a>
</div>

<br />

Karma是一种全新的数据洞察方式，用一句话表述就是**可执行的脑图**，多年前我曾向Sean Ellis（增长黑客之父）介绍了Karma的设计，他认为，Karma的模型是最好的企业数据分析方法。Karma有助于确定要进行实验的领域，也有助于从实验中得到结论。   

Karma将大大释放数据工程师和产品的工作，我们曾因为一些数据看板类的工作而投入大量前后端研发资源。而业务演进本身则迭代缓慢。数据固然重要，但投入大量资源则显得本末倒置，我们迫切需要一个这样的工具释放生产力。  

Karma填补了现有分析能力缺失的一环，和一些传统人群分析系统不同的是，传统分析聚焦某个确定的人群，基于这个人群运用各种分析手段得出结论。但这些分析没有建立数据之间联系，也缺乏面向业务的语义，比如业务想知道A活动真的带来了留资的提升吗，发出去的短信/push带来了多少曝光，这些用户后续n天的行为如何?参与某活动之前X天和之后X天用户的成交UV和GMV、核销、客单价如何变化等等。  

Karma将对数据的控制权和解释权交还给业务，最后采用何种方式得到何种结论，业务说了算。这是我认为Karma最为重要的特点，让业务探索问题并决策。  
 
## 配置数据源

点击左上角，进入数据源配置窗口，点击右上角新增图标

  <img width="800" src="https://github.com/CyrilFeng/karma/blob/main/src/main/resources/static/img/guide1.png" />

这里定义在数据源中配置的内容是一段SQL，这个SQL原则上是单表查询即

```SQL
Select col1,col2,col3... from your_table where ...
```

这种简单结构。比如：

```SQL
select
  task_id,
  exe_id,
  brand,
  user_code uid
from
  ods_tmall_prod.task_branch_sale_1h_a
where
  datetime = 'latest'
  and brand = '${brand}'
  and task_id = ${taskid}
```

越简单越好。Karma底层是基于Trino实现的，所以这里的SQL也是Trino的语法。

<table style="width:800px">
<tr>
  <td style="width:400px"></td> <td style="width:400px;">

这里【编码】为英文，【名称】随意  
【类型】目前支持用户和销售，即你的SQL是用来分析用户还是销售。  
Karma会自动感知SQL中的字段，并将它们解析到下面的【字段】模块，你可以配置字段的名称，也可以不配置。这个名称用于结果的展示。  
配置【字段类型】，这里主要是为了区分文本和数字。  
在SQL中，你可以用 ${XXX} 这样的格式表示变量，Karma会自动感知SQL中的变量，并将它们解析到下面的【参数】模块，你可以配置变量的名称、类型、以及格式（主要用于日期类型，如 yyyyMMddHH）

    
  </td>
</tr>
 

</table>

