FROM ham.apache.php8:latest
RUN echo 4.0.2-SNAPSHOT

# Copy the source files
COPY core/ /htdocs/