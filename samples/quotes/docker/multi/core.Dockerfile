FROM ham.apache.php8:latest
RUN echo samples.core version-4.1.5

# Copy the source files
COPY core/ /htdocs/