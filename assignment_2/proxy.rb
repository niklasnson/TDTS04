require 'socket'
require 'uri'
require 'pry'

class Proxy
  attr_reader :port, :debug
  def initialize(port=2442, logger=false)
    @port= port
  end

  def run(port=@port)
    begin
      @socket= TCPServer.new port
      loop do
        socket= @socket.accept
        handle_incoming_request socket
      end
    ensure
      if @socket
        @socket.close
      end
    end
  end

  # modify the request from the browser/client to remove encoding type and 
  # to use closed connection type. if request is for an allowed url foward 
  # to server and go to the listening and filter function.
  def handle_incoming_request(socket)
    begin
      request = modify_request(socket.recv(2048))
      if allowed_url?(request)
        uri= URI(request[/GET (.*) HTTP/,1])
        external_socket = Proxy.open_connetion(uri.host, uri.port)
        send_to(external_socket, request)
        filter_or_pass_through(socket, external_socket, request)
      else
        make_redirect(socket, external_socket, error='url')
      end
    rescue
      socket.close
    end
  end


  # used to send data to a socket.
  def send_to(socket, data)
    begin
      socket.write(data)
    rescue
      String.new
      socket.close
    end
  end

  # used to recive data from a socket.
  def receive_from(socket, size=2048)
    if socket
      received_data = String.new 
      transmission = String.new 
      loop do 
       transmission = socket.recv(size) 
       break if transmission.empty?
        received_data << transmission
      end 
      return received_data
    end 
  end

  # used when we don't want to filter content (images and other data). 
  def pass_through_data(client, server)
    loop do
      buffert = receive_from(server, 100000)
      break if buffert.empty?
      send_to(client, buffert)
    end
    client.close
    server.close
  end

  # recive the first part of the response and check if content is of text type 
  # and that we are looking at a GET request, else we use the pass_through_data 
  # function. 
  def filter_or_pass_through(client, server, request)
    received_data = String.new
    # get the header of the response.
    received_data += receive_from(server, 1000)

   if allowed_url?(received_data)
      if (is_text_content?(received_data) && is_get_request?(request))
        received_data += receive_from(server)

        if contains_blocked_content?(received_data)
          make_redirect(client, server, error='content')
        else
          send_to(client, received_data)
        end
      else
        send_to(client, received_data)
        pass_through_data(client, server)
      end
   else
     make_redirect(client, server, error='url')
   end
  end

  def is_get_request?(data)
    /^GET.*/.match(data) ? true : false
  end

  def is_text_content?(data)
    /Content-Type: (text\/html|text\/plain)/.match(data) ? true : false
  end

  # if content include forbidden words.
  def contains_blocked_content?(received_data=String.new)
    received_data.scan(/spongebob|britney spears|norrk.ping|paris hilton/i) { return true  }
    return false
  end

  # if url include forbidden words.
  def allowed_url?(request_data=String.new)
    request_data.scan(/spongebob|britneyspears|norrk.ping|parishilton/i) { return false  }
    return true
  end

  def modify_request(request)
    request = remove_encoding_header(request)
    request = modify_connection_type(request)
  end

  def make_redirect(client, server, error='content')
    url_error = "http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error1.html"
    content_error = "http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error2.html"
    request = "HTTP/1.1 301 Moved Permanently\r\nLocation: #{error=='content' ? content_error : url_error}\r\n"
    send_to(client, request)
    client.close if client 
    server.close if server
  end

  private

    def self.encode(str)
      str.force_encoding('UTF-8')
    end

  def self.open_connetion(host, port)
      TCPSocket.new(host, port)
    end

    # cut the Accept-Encoding part on the request.
    def remove_encoding_header(request)
      pattern = /Accept-Encoding: .*\r\n/
      request.sub(pattern, '')
    end

    # always use the Connection: close on the request.
    def modify_connection_type(request)
      pattern = /Connection: keep-alive/
      request.sub(pattern, 'Connection: close')
    end
end

if ARGV.empty?
  $port= 2442
elsif ARGV.size == 1
  $port= ARGV[0]
end

proxy = Proxy.new($port)
proxy.run
