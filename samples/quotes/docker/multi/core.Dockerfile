FROM ham.apache.php8:latest
RUN echo 4.1.0

# Copy the source files
COPY core/ /htdocs/