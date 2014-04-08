COFFEE = coffee --compile
ODIR = build
UGLIFY = uglifyjs

all :
	@rm -rf $(ODIR)
	@mkdir $(ODIR)
	@$(COFFEE) --output $(ODIR)/ .
	@mvn -f java/threadjs clean install
	@cp LICENSE $(ODIR)/
	@cp README.md $(ODIR)/

dist :
	@find $(ODIR) -name "*.js" | xargs -L 1 ./uglify.sh

clean :
	@rm -rf $(ODIR)

