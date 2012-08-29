# Force using the same RPM properties as EL5
%global _source_filedigest_algorithm 1
%global _binary_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

Name:           obm-release
Version:        2.4
Release:        1
Summary:        Open Business Management repository configuration
Group:          System Environment/Base
License:        AGPLv3

# This is a Red Hat maintained package which is specific to
# our distribution.  Thus the source is only available from
# within this srpm.
URL:            http://www.obm.org
Source1:        LICENSE
Source2:        obm.repo

BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildArch:     noarch

%description
This package contains the OBM repository configuration for yum.

%prep
%setup -q  -c -T
install -pm 644 %{SOURCE1} .

%build


%install
rm -rf $RPM_BUILD_ROOT

install -dm 755 $RPM_BUILD_ROOT%{_sysconfdir}/yum.repos.d
install -pm 644 %{SOURCE2} \
    $RPM_BUILD_ROOT%{_sysconfdir}/yum.repos.d

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
%doc LICENSE
%config /etc/yum.repos.d/*

%changelog
* Wed Aug 28 2012 Erwan Queffelec <equeffelec@linagora.com> - 2.4-1
	Initial release.
