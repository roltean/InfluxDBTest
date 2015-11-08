#!/usr/bin/env bash  
#  
# Install Ansible inside the Vagrant Ubuntu VM  
sudo apt-get update  
sudo apt-get install -y ansible  
cp /home/vagrant/environments/hosts /home/vagrant/  
chmod 666 /home/vagrant/hosts  