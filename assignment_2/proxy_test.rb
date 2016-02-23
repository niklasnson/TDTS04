require './proxy'
require 'test/unit'

class TestProxy < Test::Unit::TestCase

  def test_blocked_content
    p = Proxy.new
    assert_equal(["spongebob", "britney spears", "paris hilton", "norrköping"], p.blocked_content)
    assert_equal(["spongebob", "britneyspears", "parishilton", "norrköping"], p.blocked_content(true))
  end

  def test_filter_or_pass_through
    response = "HTTP/1.1 200 OK\r\nDate: Mon, 01 Feb 2016 14:58:38 GMT\r\nServer: Apache/2.2.24 (Unix) DAV/2 SVN/1.6.17 PHP/5.3.23 mod_fastcgi/2.4.6 mod_auth_kerb/5.4+ida mod_jk/1.2.31 mod_ssl/2.2.24 OpenSSL/0.9.7d\r\nLast-Modified: Mon, 21 Jan 2008 07:14:16 GMT\r\nETag: \"184802-77-444363d68220b\"\r\nAccept-Ranges: bytes\r\nContent-Length: 119\r\nConnection: close\r\nContent-Type: text/plain\r\n\r\n\nThis is a plain text file with no bad words in it.\n\nYour Web browser should be able to display this page just fine.\n\n\n"
  end

  def test_is_text_content
    text_plain = "HTTP/1.1 200 OK\r\nDate: Mon, 01 Feb 2016 14:58:38 GMT\r\nServer: Apache/2.2.24 (Unix) DAV/2 SVN/1.6.17 PHP/5.3.23 mod_fastcgi/2.4.6 mod_auth_kerb/5.4+ida mod_jk/1.2.31 mod_ssl/2.2.24 OpenSSL/0.9.7d\r\nLast-Modified: Mon, 21 Jan 2008 07:14:16 GMT\r\nETag: \"184802-77-444363d68220b\"\r\nAccept-Ranges: bytes\r\nContent-Length: 119\r\nConnection: close\r\nContent-Type: text/plain\r\n\r\n\nThis is a plain text file with no bad words in it.\n\nYour Web browser should be able to display this page just fine.\n\n\n"
    text_html = "HTTP/1.1 200 OK\r\nDate: Mon, 01 Feb 2016 14:58:38 GMT\r\nServer: Apache/2.2.24 (Unix) DAV/2 SVN/1.6.17 PHP/5.3.23 mod_fastcgi/2.4.6 mod_auth_kerb/5.4+ida mod_jk/1.2.31 mod_ssl/2.2.24 OpenSSL/0.9.7d\r\nLast-Modified: Mon, 21 Jan 2008 07:14:16 GMT\r\nETag: \"184802-77-444363d68220b\"\r\nAccept-Ranges: bytes\r\nContent-Length: 119\r\nConnection: close\r\nContent-Type: text/html\r\n\r\n\nThis is a plain text file with no bad words in it.\n\nYour Web browser should be able to display this page just fine.\n\n\n"
    p = Proxy.new
    assert_equal(true, p.is_text_content?(text_plain))
    assert_equal(true, p.is_text_content?(text_html))
  end

  def test_allowed_url
    p = Proxy.new
    request = "GET http://www.ida.liu.se/~TDTS04/labs/2011/ass2/goodtest1.txt HTTP/1.1\r\nHost: www.ida.liu.se\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:43.0) Gecko/20100101 Firefox/43.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Language: en-US,en;q=0.5\r\nConnection: close\r\n\r\n"
    request_bad = "GET http://www.ida.liu.se/~TDTS04/labs/2011/ass2/spongebob.txt HTTP/1.1\r\nHost: www.ida.liu.se\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:43.0) Gecko/20100101 Firefox/43.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Language: en-US,en;q=0.5\r\nConnection: close\r\n\r\n"
    request_super_bad = "GET http://www.parishilton.com HTTP/1.1\r\nHost: www.ida.liu.se\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:43.0) Gecko/20100101 Firefox/43.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Language: en-US,en;q=0.5\r\nConnection: close\r\n\r\n"
    request_ultra_bad = "GET http://www.britneyspears.com HTTP/1.1\r\nHost: www.ida.liu.se\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:43.0) Gecko/20100101 Firefox/43.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Language: en-US,en;q=0.5\r\nConnection: close\r\n\r\n"
    assert_equal(true, p.allowed_url?(request))
    assert_equal(false, p.allowed_url?(request_bad))
    assert_equal(false, p.allowed_url?(request_super_bad))
    assert_equal(false, p.allowed_url?(request_ultra_bad))
  end

  def test_is_get_request
    p = Proxy.new
    request = "GET http://www.ida.liu.se/~TDTS04/labs/2011/ass2/goodtest1.txt HTTP/1.1\r\nHost: www.ida.liu.se\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:43.0) Gecko/20100101 Firefox/43.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Language: en-US,en;q=0.5\r\nConnection: close\r\n\r\n"
    bad_request = "PUT http://www.ida.liu.se/~TDTS04/labs/2011/ass2/goodtest1.txt HTTP/1.1\r\nHost: www.ida.liu.se\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:43.0) Gecko/20100101 Firefox/43.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Language: en-US,en;q=0.5\r\nConnection: close\r\n\r\n"
    assert_equal(true, p.is_get_request?(request))
    assert_equal(false, p.is_get_request?(bad_request))
  end

  def test_modify_request
    response = "GET http://www.ida.liu.se/~TDTS04/labs/2011/ass2/goodtest1.txt HTTP/1.1\r\nHost: www.ida.liu.se\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:43.0) Gecko/20100101 Firefox/43.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Language: en-US,en;q=0.5\r\nConnection: close\r\n\r\n"
  end


  def test_super_test 
    request = "GET http://www.ida.liu.se/~TDTS04/labs/2011/ass2/goodtest1.txt HTTP/1.1\r\nHost: www.ida.liu.se\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:43.0) Gecko/20100101 Firefox/43.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Language: en-US,en;q=0.5\r\nConnection: close\r\n\r\n"

    post_request = "POST http://ocsp.digicert.com/ HTTP/1.1\r\nHost: ocsp.digicert.com\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:43.0) Gecko/20100101 Firefox/43.0\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\nAccept-Language: en-US,en;q=0.5\r\nContent-Length: 83\r\nContent-Type: application/ocsp-request\r\nConnection: close\r\n\r\n0Q0O0M0K0I0\t\x06\x05+\x0E\x03\x02\x1A\x05\x00\x04\x14\x10_\xA6z\x80\b\x9D\xB5'\x9F5\xCE\x83\vC\x88\x9E\xA3\xC7\r\x04\x14\x0F\x80a\x1C\x821a\xD5/(\xE7\x8DF8\xB4,\xE1\xC6\xD9\xE2\x02\x10\t\xA1\xA7\x02\x01\x10\xDCb\xE6:\x9B\xFBs\x1C\xF1("

    response = "HTTP/1.1 200 OK\r\nDate: Tue, 02 Feb 2016 05:32:06 GMT\r\nServer: Apache/2.2.24 (Unix) DAV/2 SVN/1.6.17 PHP/5.3.23 mod_fastcgi/2.4.6 mod_auth_kerb/5.4+ida mod_jk/1.2.31 mod_ssl/2.2.24 OpenSSL/0.9.7d\r\nLast-Modified: Mon, 21 Jan 2008 07:14:16 GMT\r\nETag: \"184802-77-444363d68220b\"\r\nAccept-Ranges: bytes\r\nContent-Length: 119\r\nConnection: close\r\nContent-Type: text/plain\r\n\r\n\nThis is a plain text file with no bad words in it.\n\nYour Web browser should be able to display this page just fine.\n\n\n"

    p = Proxy.new
    assert_equal(true, p.is_text_content?(response)) 
  end

  def test_contains_bad_strings
    p = Proxy.new
    assert_equal(true, Filter.contains_bad_strings?("norrköping",p.blocked_content)) 
    assert_equal(true, Filter.contains_bad_strings?("britney spears",p.blocked_content)) 

  end 
  
end
