FROM ham.apache.php8:latest
RUN echo samples.core version-4.3.1

# Copy the source files
COPY core/ /htdocs/