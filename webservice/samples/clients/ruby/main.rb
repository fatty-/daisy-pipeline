#!/usr/bin/env ruby
require 'rubygems'
require 'optparse'
require 'nokogiri'
require './settings'
require './resources'

def main

  checkargs

  if Settings.instance.command == "scripts"
    get_scripts
  elsif Settings.instance.command == "script"
    get_script(Settings.instance.options[:id])
  elsif Settings.instance.command == "jobs"
    get_jobs
  elsif Settings.instance.command == "job"
    get_job(Settings.instance.options[:id])
  elsif Settings.instance.command == "log"
    get_log(Settings.instance.options[:id])
  elsif Settings.instance.command == "result"
    get_result(Settings.instance.options[:id])
  elsif Settings.instance.command == "delete-job"
    delete_job(Settings.instance.options[:id])
  elsif Settings.instance.command == "new-job"
    post_job(Settings.instance.options[:request], Settings.instance.options[:job_data])
  elsif Settings.instance.command == "clients"
	  get_clients
	elsif Settings.instance.command == "client"
	  get_client(Settings.instance.options[:id])
	elsif Settings.instance.command == "new-client"
	  post_client(Settings.instance.options[:request])
	elsif Settings.instance.command == "update-client"
    put_client(Settings.instance.options[:id], Settings.instance.options[:request])	
	elsif Settings.instance.command == "delete-client"
		delete_client(Settings.instance.options[:id])
  elsif Settings.instance.command == 'halt'
    halt(Settings.instance.options[:id])
  else
    puts "Command #{Settings.instance.command} not recognized"
  end

end

def checkargs

  optparse = OptionParser.new do |opts|

  opts.banner = "

  Usage: main.rb command options

  Commands:

  scripts       List all scripts.
  script        List details for a single script.
  jobs          List all jobs.
  job           List details for a single job.
  log           Show the log for a job.
  result        Show where the result is stored for a job.
  delete-job    Delete a job.
  new-job       Create a new job.
  clients       List all clients (admin only).
  client        List details for a single client.
  delete-client Delete a client.
  new-client    Create a new client.
  update-client Provide new information for an existing client.
  halt          Stop the web service

  Examples:
  Show all scripts:
	  main.rb scripts
  Show a specific script:
	  main.rb script --id=http://www.daisy.org/pipeline/modules/dtbook-to-zedai/dtbook-to-zedai.xpl
  Show a specific job:
	  main.rb job --id=873ce8d7-0b92-42f6-a2ed-b5e6a13b8cd7
  Create a job:
	  main.rb new-job --request=../testdata/job1.request.xml
  Create a job:
  	main.rb new-job --request=../testdata/job2.request.xml --job-data=../testdata/job2.data.zip
  Create a client:
    main.rb new-client --request=../testdata/client.request.xml
  Update a client:
    main.rb update-client --id=theId --request=../testdata/client.update.xml
  Delete a client:
    main.rb delete-client --id=theId
  "

    Settings.instance.options[:id] = nil
    opts.on('--id VALUE', 'ID of Job or Script or Client') do |val|
      Settings.instance.options[:id] = val
    end

		Settings.instance.options[:job_data] = nil
      opts.on('--job-data VALUE', 'Zip file containing the extra job data (e.g. non-xml files that are part of a book)') do |val|
        Settings.instance.options[:job_data] = val
    end

    Settings.instance.options[:request] = nil
      opts.on('--request VALUE', 'XML file representing the resource creation request') do |val|
        Settings.instance.options[:request] = val
    end


    opts.on('--help', 'Display this screen') do
      puts opts
      exit
    end
  end

  begin
    optparse.parse!
  rescue
    puts "Error parsing options"
    return
  end

  # only expecting one command so just grab the first one
  ARGV.each do |a|
    Settings.instance.command = a
    break
  end
end

def get_scripts
  doc = Resources.get_scripts
  if doc == nil
    return
  end
  puts doc.to_xml(:indent => 2)
end

def get_script(id)
  if id == ""
    puts "ID required"
    return
  end
  doc = Resources.get_script(id)
  if doc == nil
    puts "No data returned"
    return
  end
  puts doc.to_xml(:indent => 2)
end

def get_jobs
  doc = Resources.get_jobs
  if doc == nil
    puts "No data returned"
    return
  end
  puts doc.to_xml(:indent => 2)
end

def get_job(id)
  if id == nil
    puts "ID required"
    return
  end
  doc = Resources.get_job(id)
  if doc == nil
    puts "No data returned"
    return
  end
  puts doc.to_xml(:indent => 2)
end

def get_log(id)
  if id == nil
    puts "ID required"
    return
  end
  status = Resources.get_job_status(id)
  if status != "DONE"
    puts "Cannot get log until the job is done. Job status: #{status}."
    return
  end
  
  log = Resources.get_log(id)
  if log == ""
    puts "No data returned"
    return
  end
  puts log
end

def get_result(id)
  if id == nil
    puts "ID required"
    return
  end
  status = Resources.get_job_status(id)
  if status == ""
    puts "No data returned"
    return
  end
  if status != "DONE"
    puts "Cannot get result until the job is done. Job status: #{status}."
    return
  end

  response = Resources.get_result(id)
  if response == nil
    puts "No data returned"
    return
  end
  path = "/tmp/#{id}.zip"
  open(path, "wb") { |file|
    file.write(response)
  }
  puts "Saved to #{path}"
end

def get_clients
	doc = Resources.get_clients
  if doc == nil
    puts "No data returned"
    return
  end
  puts doc.to_xml(:indent => 2)
end

def get_client(id)
	if id == ""
    puts "ID required"
    return
  end
  doc = Resources.get_client(id)
  if doc == nil
    puts "No data returned"
    return
  end
  puts doc.to_xml(:indent => 2)
end

def post_job(job_request_filepath, job_data_filepath)
  if job_request_filepath == nil
    puts "--request filepath required"
    return
  end

  if !File.readable?(job_request_filepath)
    puts "Invalid request filepath"
    return
  end
  
  request = File.read(job_request_filepath)
  doc = nil
  
  if job_data_filepath == nil
    doc = Resources.post_job(request, nil)
  else
    if !File.readable?(job_data_filepath)
      puts "Invalid job data filepath"
      return
    end
    data = File.open(job_data_filepath, "rb")
    doc = Resources.post_job(request, data)
    data.close
  end
  
  if doc == nil
    puts "No data returned"
    return
  end
  puts doc.to_xml(:indent => 2)  
end

def post_client(request_filepath)
  if request_filepath == nil
    puts "--request filepath required"
    return
  end

  if !File.readable?(request_filepath)
    puts "Invalid request filepath"
    return
  end

  request = File.read(request_filepath)
  doc = Resources.post_client(request)

  if doc == nil
    puts "Error creating client"
    return
  end
  puts doc.to_xml(:indent => 2)
end

def put_client(id, request_filepath)
	if id == nil
    puts "ID required"
    return
  end

  if request_filepath == nil
    puts "--request filepath required"
    return
  end

  request = File.read(request_filepath)
  doc = Resources.put_client(id, request)

  if doc == nil
    puts "Error updating client"
    return
  end
  puts doc.to_xml(:indent => 2)
end

def delete_job(id)
  if id == ""
    puts "ID required"
    return
  end

  status = Resources.get_job_status(id)
  if status == ""
    puts "No data returned"
    return
  end
  if status != "DONE"
    puts "Cannot delete until the job is done. Job status: #{status}."
    return
  end

  result = Resources.delete_job(id)
  if result
    puts "Job deleted"
  else
    puts "Error deleting job"
  end

end

def delete_client(id)
  if id == ""
    puts "ID required"
    return
  end

	result = Resources.delete_client(id)
  if result
    puts "Client deleted"
  else
    puts "Error deleting client"
  end

end

def halt(id)
  if id == ""
    puts "ID required (find the value in the key.txt file generated when the WS started)"
    return
  end
  
  result = Resources.halt(id)
  if result != nil
    puts "Success"
  else
    puts "Failure"
  end
end

# execution starts here
main 

