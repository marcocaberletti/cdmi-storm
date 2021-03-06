package it.grid.storm.cdmi.auth.impl;

import com.google.common.collect.Lists;

import it.grid.storm.cdmi.auth.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class IamUser implements User {

  private String sub;
  private List<String> scopes;
  private List<String> groups;
  private String organization;
  private List<GrantedAuthority> authorities;

  /**
   * Constructor.
   * 
   * @param auth Build @IamUser from details of @UsernamePasswordAuthenticationToken auth.
   */
  public IamUser(UsernamePasswordAuthenticationToken auth) {

    sub = null;
    organization = null;
    scopes = Lists.newArrayList();
    groups = Lists.newArrayList();
    authorities = Lists.newArrayList();

    if (auth.getPrincipal() instanceof UserDetails) {

      UserDetails user = (UserDetails) auth.getPrincipal();
      sub = user.getUsername();
      authorities.addAll(user.getAuthorities());

    } else {

      try {

        JSONObject authDetails = new JSONObject(auth.getDetails().toString());
        sub = authDetails.getJSONObject("userinfo").getString("sub");
        authDetails.getJSONObject("userinfo").getJSONArray("groups")
            .forEach(g -> this.groups.add(g.toString()));
        String scopesStr = authDetails.getJSONObject("tokeninfo").getString("scope");
        if (!scopesStr.isEmpty()) {
          for (String scope : scopesStr.split(" ")) {
            scopes.add(scope);
          }
        }
        organization = authDetails.getJSONObject("tokeninfo").getString("organisation_name");
        authorities.addAll(auth.getAuthorities());

      } catch (JSONException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }

  }

  @Override
  public String getUserId() {

    return sub;
  }

  @Override
  public List<String> getScopes() {

    return scopes;
  }

  @Override
  public boolean hasScope(String scope) {

    return scopes.contains(scope);
  }

  @Override
  public List<String> getGroups() {

    return groups;
  }

  @Override
  public boolean hasGroup(String group) {

    return groups.contains(group);
  }

  @Override
  public String getOrganizationName() {

    return organization;
  }

  @Override
  public List<GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String toString() {
    return "IamUser [sub=" + sub + ", scopes=" + scopes + ", groups=" + groups + ", organization="
        + organization + ", authorities=" + authorities + "]";
  }

  @Override
  public boolean hasAuthority(GrantedAuthority authority) {
    for (GrantedAuthority auth : authorities) {
      if (auth.getAuthority().equals(authority.getAuthority())) {
        return true;
      }
    }
    return false;
  }
}
