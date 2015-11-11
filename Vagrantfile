# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  if Vagrant.has_plugin?("vagrant-proxyconf")
	  config.proxy.http = "http://192.168.1.112:3128"
	  config.proxy.https = "http://192.168.1.112:3128"
	  config.proxy.no_proxy = "localhost,127.0.0.1,192.168.0.10,192.168.0.11,192.168.0.12"
  end

  config.vm.define "influxdb-grafana-ubuntu" do |ubuntu|
    ubuntu.vm.box = "ubuntu/trusty64"

	# ubuntu.vm.synced_folder './roles', '/roles', mount_options: ["fmode=666"]
	ubuntu.vm.synced_folder 'provisioning/ansible/environments', '/home/vagrant/environments', mount_options: ["fmode=666"]
	
	ubuntu.vm.provision :shell, :path => "provisioning/shell/install-ansible.sh"  
    ubuntu.vm.provision :shell, :path => "provisioning/shell/provision-master.sh"

    ubuntu.vm.network :forwarded_port, guest: 8086, host: 8086
    ubuntu.vm.network :forwarded_port, guest: 8083, host: 8083
    ubuntu.vm.network :forwarded_port, guest: 3000, host: 3000

    #ubuntu.vm.provision "ansible" do |ansible| 
    #  ansible.playbook = "dashboard.yml"
    #end 
  end

  config.vm.provider "virtualbox" do |v|
    v.customize ["modifyvm", :id, "--memory", "1024"]
  end

end
