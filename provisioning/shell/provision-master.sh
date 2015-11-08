#!/usr/bin/env bash

sudo ansible-playbook /vagrant/provisioning/ansible/install.yml -i /home/vagrant/environments/hosts --connection=local