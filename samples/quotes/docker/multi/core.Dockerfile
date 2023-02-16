FROM ham.apache.php8:latest
RUN echo samples.core version-4.1.6

# Copy the source files
COPY core/ /htdocs/