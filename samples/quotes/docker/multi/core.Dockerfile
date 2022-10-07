FROM ham.apache.php8:latest
RUN echo 3.1.1

# Copy the source files
COPY core/ /htdocs/