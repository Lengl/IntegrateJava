# IntegrateJava
Integrating project on Java

This one is also done for integrating f(x)=sin(1/x)*sin(1/x)/(x*x) on multiple cores.
Here are some results on my local machine:

threads: 1 ideal capacity of local stack: 30 (0 drops of local stack)
7107, 7909, 7835, 7830, 6913, 7785, 7247
average:	7518

threads: 2 ideal capacity: 15
4222, 4288, 4121, 4127, 4135
average:	4179
speed-up:	1.80
efficiency:	90%

threads: 3 ideal capacity: 15
3297, 3505, 3389, 3376, 3333
average:	3380
speed-up:	2.22
efficiency:	74%

By "ideal capacity" I mean the biggest one when all threads are almost equally busy. I find it by manual changes & tests. 
