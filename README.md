**_Lambda:_**
KafkaProducerLambda is a java based Lambda triggered by an APIGateway using a POST request such as 
https://oca2gcluz9.execute-api.eu-west-2.amazonaws.com/Prod/send. At the moment there is no expected request body.


KafkaProducerLambda calls KafkaProducerService to send messages to a Kafka topic

KafkaProducerService instantiates KafkaProducer to send messages to kafka topic
The APIGateway is configured inside the SAM yaml template in this project.

**_Configure/Create VPC and EC2 instances aws:_**

1. Create a VPC with two subnets.
2. One subnet will be a private subnet and the other one a public.
3. Route table of the private subnet within this VPS should point to a NAT gateway and also VPS endpoint
    e.g.10.0.0.0/16	    local
        0.0.0.0/0	    nat-0443622c668fd171d
        pl-7ca54015	    vpce-0644710a43eb6a589
4.VPC endpoint configuration:
awskafka_vpc-rtb-private1-eu-west-2a	rtb-0778f82789db2d55c (awskafka_vpc-rtb-private1-eu-west-2a)	No	subnet-0762170e4a704613a (awskafka_vpc-subnet-private1-eu-west-2a)

5. NAT Gateway should point to the public subnet within this VPC
6. Create one EC2 INSTANCE with the above VPC and the above mentioned private subnet. 
    Kafka will be installed on this EC2 instance. So this will be the **kafka server**.
7. Create one more EC2 INSTANCE with the above VPC and the above mentioned public subnet. 
    This will act as your **bastion server**. You will ssh into this bastion server from your home computer.
    From this you can ssh into the EC2 instance on private subnet.
8. 
8.  Access Kafka server via bastion server
    1. On your home computer, on the terminal, type in
    8.1 ssh-add <your aws ssh pem key> 
    8.2 ssh-add -L to verify
    8.3 ssh into aws bastion server: ssh -v -A ec2-user@<public ip of bastion server>
    8.4 From your bastion server, do ssh -v ec2-user@<private ip of kafka server>
    
**_Install/Configure Apache Kafka/Zookeeper:_**
   
9. Install Java
   sudo yum apt update
   sudo amazon-linux-extras install java-openjdk11
   Download and Extract Apache Kafka
   wget https://archive.apache.org/dist/kafka/3.0.0/kafka_2.13-3.0.0.tgz
   tar xzf kafka_2.13-3.0.0.tgz
   mv kafka_2.13-3.0.0 /usr/local/kafka
   Our next steps will be to create systemd unit files for the Zookeeper and Kafka. 
   This will help us to manage Kafka/Zookeeper to run as services using the systemctl command.
   
   Setup Zookeeper Systemd Unit File. Run the command:
   vim /etc/systemd/system/zookeeper.service
   Paste below content:
   
   [Unit]
   Description=Apache Zookeeper server
   Documentation=http://zookeeper.apache.org
   Requires=network.target remote-fs.target
   After=network.target remote-fs.target
   
   [Service]
   Type=simple
   ExecStart=/usr/local/kafka/bin/zookeeper-server-start.sh /usr/local/kafka/config/zookeeper.properties
   ExecStop=/usr/local/kafka/bin/zookeeper-server-stop.sh
   Restart=on-abnormal
   
   [Install]
   WantedBy=multi-user.target
   Save and exit
   
   Setup Kafka Systemd Unit File. Run the command:
   vim /etc/systemd/system/kafka.service
   Paste below content:
   
   [Unit]
   Description=Apache Kafka Server
   Documentation=http://kafka.apache.org/documentation.html
   Requires=zookeeper.service
   
   [Service]
   Type=simple
   Environment="JAVA_HOME=/usr/lib/jvm/java-11-openjdk-11.0.13.0.8-1.amzn2.0.3.x86_64/"
   Environment="KAFKA_HEAP_OPTS=-Xmx256M -Xms256M"
   ExecStart=/usr/local/kafka/bin/kafka-server-start.sh /usr/local/kafka/config/server.properties
   ExecStop=/usr/local/kafka/bin/kafka-server-stop.sh
   
   [Install]
   WantedBy=multi-user.target
   Save and exit
   
   Note: The environment fields above will depend on your setup. For my case, 
   I use a free-tier EC2 instance type thus limited memory which explains the value for KAFKA_HEAP_OPTS. 
   Also, the JAVA_HOME value will depend on your JDK installation path. 
   One can check this by running the command update-alternatives --config java.
   
   Reload the systemd daemon to apply new changes.
   systemctl daemon-reload
   Start Zookeeper Server
   sudo systemctl start zookeeper
   One can check the status if active by running below command:
   
   sudo systemctl status zookeeper
   Start Kafka Server
   sudo systemctl start kafka
   One can check the status if active by running below command:
   
   sudo systemctl status kafka
   Create a topic in Kafka. 
   cd /usr/local/kafka/bin
   ./kafka-topics.sh --create --bootstrap-server localhost:9092 --topic topic1 --replication-factor 1 --partitions 1

**_build and deploy lambda:_**  
10. Update the SAM template yaml file in this project with your lambda name, package name etc and the value for 
the environment variable KAFKA_BROKER_LIST. This should point to <your private IP of the Kafka server:9092>

11. Using the SAM template yaml file in this project build and deploy lambda to aws

12. Now go to Permissions tab and click the role name to open the IAM console. 
In addition to the existing AWSLambdaBasicExecutionRole policy, 
add a new inline policy with a name SelfHostedKafkaPolicy with the following permissions:
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "ec2:CreateNetworkInterface",
                    "ec2:DescribeNetworkInterfaces",
                    "ec2:DescribeVpcs",
                    "ec2:DeleteNetworkInterface",
                    "ec2:DescribeSubnets",
                    "ec2:DescribeSecurityGroups",
                    "logs:CreateLogGroup",
                    "logs:CreateLogStream",
                    "logs:PutLogEvents"
                ],
                "Resource": "*"
            }
        ]
    }
    then save.
    
13. Go to VPC tab of your lambda and edit VPC details of lambda. Point to the above VPC and private subnet. 
This will allow lambda to access the kafka server

14. Using APIGateway test your App!





