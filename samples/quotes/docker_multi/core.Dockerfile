FROM ham.base:ham.apache.php8:latest

# Copy the source files
COPY core/ /htdocs/