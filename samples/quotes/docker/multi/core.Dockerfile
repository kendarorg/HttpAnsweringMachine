FROM ham.apache.php8:latest
RUN echo 4.1.3-SNAPSHOT

# Copy the source files
COPY core/ /htdocs/