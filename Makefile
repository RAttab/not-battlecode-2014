#------------------------------------------------------------------------------#
# Makefile
# Rémi Attab (remi.attab@gmail.com), 11 Jan 2013
# FreeBSD-style copyright and disclaimer apply
#------------------------------------------------------------------------------#

OS := $(shell uname)

ifeq ($(OS), Darwin) # Mac OS X
	INSTALL_DIR ?= /Applications/Battlecode2014
else
	INSTALL_DIR ?= ~/Battlecode2014
endif

#INSTALL_DIR := ./tmp

TEAM_DIR := $(INSTALL_DIR)/teams
BIN_DIR := $(INSTALL_DIR)/bin

all: install-bots install-maps

BOTS := blahbot

install-bots: $(foreach bot,$(BOTS),$(INSTALL_DIR)/teams/$(bot))
$(INSTALL_DIR)/teams/%:
	 ln -s $(CURDIR)/$* $@

install-maps: $(foreach map,$(wildcard maps/*.xml),$(INSTALL_DIR)/$(map))
$(INSTALL_DIR)/maps/%:
	ln -s $(CURDIR)/maps/$* $@

