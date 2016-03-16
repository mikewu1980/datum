-- MySql 实现 Oracle 的分析函数——row_number() over(partition by )

select 
	empid, deptid, salary, rank 
from (
	select 
		v.empid, v.deptid, v.salary, @rownum := @rownum + 1, 
		if(@group = v.deptid, @rank := @rank + 1, @rank := 1) as rank, 
		@group := v.deptid 
	from (  
		select empid, deptid, salary from employee order by deptid asc, salary desc
	) v,
	(select @rownum := 0, @group := null, @rank := 0) a 
) result;
