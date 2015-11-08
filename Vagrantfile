# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.define "influxdb-grafana-ubuntu" do |ubuntu|
    ubuntu.vm.box = "ubuntu/trusty64"
    ubuntu.vm.network :private_network, ip: "192.168.33.21"

	# ubuntu.vm.synced_folder './roles', '/roles', mount_options: ["fmode=666"]
	ubuntu.vm.synced_folder 'provisioning/ansible/environments', '/home/vagrant/environments', mount_options: ["fmode=666"]
	
	ubuntu.vm.provision :shell, :path => "provisioning/shell/install-ansible.sh"  
    ubuntu.vm.provision :shell, :path => "provisioning/shell/provision-master.sh" 

    #ubuntu.vm.provision "ansible" do |ansible| 
    #  ansible.playbook = "dashboard.yml"
    #end 
  end

  config.vm.provider "virtualbox" do |v|
    v.customize ["modifyvm", :id, "--memory", "1024"]
  end

end
