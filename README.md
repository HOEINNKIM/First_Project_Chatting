# First Project ¡¸ò±ªêùêª¤¡¹  

## It means 'Let's start from acquaintance'  
## Period : 3 weeks  
## Tech : Pure Java 8, Java fx, Mybatis, Oracle DB 11g  

## Settings  
1. Open 'sqlCodesSetting.txt' in src folder  
2. Copy the SQL codes and run it(Used sqldeveloper in my case).  
3. Modify 'db.properties' to your DB configuration settings.  
4. Go to 'src > user > ui'  
5. UserClientMain.java > 'InetSocketAddress' > Modify ip Number to yours.  
6. UserServerMain.java > 'InetSocketAddress' > Modify ip Number to yours.  
7. If it is windows, you need to create new rules 'inbound rules', 'outbound rules' from Windows Defender > Detail Settings  
8. Create '5001' TCP port rules for both inbound and outbound rule  
10. Run UserServerMain > start server  
11. Run UserClientMain > request connect  
12. Ready to start.  