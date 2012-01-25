require './rest'
module Resources
  module_function
  
  def get_scripts
    uri = "#{Settings::BASEURI}/scripts"
    doc = Rest.get_resource_as_xml(uri)
    return doc
  end

  def get_script(id)
    # escape the ID for now since it's actually a URI
    escaped_id = CGI.escape(id)
    uri = "#{Settings::BASEURI}/scripts/#{escaped_id}"
    doc = Rest.get_resource_as_xml(uri)
    return doc
  end

  def get_jobs
    uri = "#{Settings::BASEURI}/jobs"
    doc = Rest.get_resource_as_xml(uri)
    return doc
  end

  def get_job(id)
    uri = "#{Settings::BASEURI}/jobs/#{id}"
    doc = Rest.get_resource_as_xml(uri)
    return doc
  end

  def get_log(id)
    uri = "#{Settings::BASEURI}/jobs/#{id}/log"
    doc = Rest.get_resource(uri)
    return doc
  end

  def get_result(id)
    uri = "#{Settings::BASEURI}/jobs/#{id}/result"
    doc = Rest.get_resource(uri)
    return doc
  end

  def post_job(request, data)
    uri = "#{Settings::BASEURI}/jobs"
    if data == nil
      job_id = Rest.post_resource(uri, request);
    else
      job_id = Rest.post_resource(uri, {"job-request"=> request, "job-data"=>data});
    end
    return job_id
  end

  def delete_job(id)
    uri = "#{Settings::BASEURI}/jobs/#{id}"
    success = Rest.delete_resource(uri)
    return success
  end

  def get_job_status(id)
    doc = get_job(id)
    if doc == nil
      return ""
    end
    doc.remove_namespaces!
    return doc.xpath(".//job")[0]['status']
  end

	def get_clients()
		uri = "#{Settings::BASEURI}/admin/clients"
		doc = Rest.get_resource_as_xml(uri)
		return doc
	end

	def get_client(id)
		uri = "#{Settings::BASEURI}/admin/clients/#{id}"
		doc = Rest.get_resource_as_xml(uri)
		return doc
	end

	def post_client(request)
		uri = "#{Settings::BASEURI}/admin/clients"
		success = Rest.post_resource(uri, request)
		return success
	end
	
	def put_client(id, request)
		uri = "#{Settings::BASEURI}/admin/clients/#{id}"
		success = Rest.put_resource(uri, request)
		return success
	end
	
	def delete_client(id)
    uri = "#{Settings::BASEURI}/admin/clients/#{id}"
    success = Rest.delete_resource(uri)
    return success
	end
end
