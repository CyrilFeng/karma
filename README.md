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

Karma是一种全新的数据洞察方式，我称它为因果洞察。也许你曾在别的地方听过因果洞察，但Karma的产品理念是独一无二的。多年前我曾向Sean Ellis（增长黑客之父）介绍了Karma的设计，他认为，Karma的模型是最好的企业数据分析方法。Karma有助于确定要进行实验的领域，也有助于从实验中得到结论。   

Karma将大大释放数据工程师和产品的工作，我们曾因为一些数据看板类的工作而投入大量前后端研发资源。而业务演进本身则迭代缓慢。数据固然重要，但投入大量资源则显得本末倒置，我们迫切需要一个这样的工具释放生产力。  

Karma填补了现有分析能力缺失的一环，也是业务迫切需要的一步。和一些传统人群分析系统不同的是，传统分析聚焦某个确定的人群，基于这个人群运用各种分析手段得出结论。但这些分析没有建立数据之间联系，也缺乏面向业务的语义，比如业务想知道A活动真的带来了留资的提升吗，发出去的短信/push带来了多少承接页的曝光，这些曝光用户后续3天的行为如何，参与某活动之前X天和之后X天用户的成交UV和GMV、核销、客单价等等如何变化等等。  

Karma将对数据的控制权和解释权交还给业务，最后采用何种方式得到何种结论，业务说了算。这是我认为Karma最为重要的特点，让业务探索问题并决策。  
 
## 配置数据源

点击左上角，进入数据源配置窗口，点击右上角新增图标

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


