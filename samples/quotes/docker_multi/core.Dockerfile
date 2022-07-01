FROM ham.apache.php8:latest
RUN echo 3.0.7-SNAPSHOT

# Copy the source files
COPY core/ /htdocs/