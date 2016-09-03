设有表结构如下(mysql 示例)
```
create table testdb.articles1
(
id int primary key,
name int,
age int，
mom clob,
des blob
)
```
则在 insert.sql 中内容可如下配置("//"右侧为注释内容，实际配置文件中注释内容将被解释为对应字段值，请删除)：
```
id#i   				//i将会被不重复的数字替换
name#test_name  		//所有name字段将会被字符串 'test_name' 替换
age#100				//所有age字段将会被数字 100 替换
mom#这将是clob字段的内容	//所有mom字段将会字符串 '这将是clob字段的内容' 替换
des#file			//所有des字段都想被指定的文件的二进制内容替换，该文件在启动jar包时手动设置 

```
### 规则说明：
"#" 左边为表字段名，大小写保持一致
"#" 右边为字段值，所有该字段都将是这个值，字符串不需要加'',不支持跨行，其中 i ，file ,time 为特殊值
其中 i 代表不重复的整型，file 代表指定文件的二进制，time为时间(仅针对oracle，其他数据库请勿使用)

基础表，假设有n个表，分别为 users1,users2,...usersn,则基础表为 users
表个数，假设基础表设为 users,并且表个数设为10,则将被操作的表为 users1,users2,...,users10

不需要将表中所有字段全部指定，除非空字段(主键等)外，其他字段均可选，如上表可如下配置：
```
id#i  			

des#file

```

在目录下执行 java -jar teclan-databasetools-xxx-SNAPSHOT.jar 即可运行


