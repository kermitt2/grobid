# Configuration of the password for the RESTful services

The default password for the administration console is **admin**.

For security, the password is saved as SHA1 hash in the file:

> grobid-home/config/grobid_service.properties

with the property name:

> org.grobid.service.admin.pw

To change the password, you can replace this property value by the SHA1 hash generated for your new password of choice. To generate the SHA1 from any "`<input_string>`", you can use the corresponding Grobid REST service available at:

> http://localhost:8080/sha1?sha1=`<input_string>`